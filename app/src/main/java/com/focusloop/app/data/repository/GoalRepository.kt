package com.focusloop.app.data.repository

import com.focusloop.app.domain.model.Goal
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"
private const val GOALS_SUBCOLLECTION = "goals"

/**
 * Goals, stored per-user at users/{uid}/goals/{goalId}. goalId is a
 * timestamp-based Long (matching the old Room auto-increment contract) so
 * existing navigation routes and intent extras that carry a Long goal id
 * didn't need to change when this moved off Room.
 */
class GoalRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val authRepository: AuthRepository
) {

    private fun goalsCollection(uid: String) =
        firestore.collection(USERS_COLLECTION).document(uid).collection(GOALS_SUBCOLLECTION)

    private fun allGoalsRaw(): Flow<List<Goal>> =
        authRepository.authState
            .map { it.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid == null) {
                    flowOf(emptyList())
                } else {
                    callbackFlow {
                        val registration = goalsCollection(uid).addSnapshotListener { snapshot, _ ->
                            trySend(snapshot?.documents?.mapNotNull { it.toGoal() } ?: emptyList())
                        }
                        awaitClose { registration.remove() }
                    }
                }
            }

    val activeGoals: Flow<List<Goal>> = allGoalsRaw().map { goals ->
        goals.filterNot { it.completed }.sortedWith(compareBy<Goal> { it.priority }.thenByDescending { it.createdAt })
    }

    val allGoals: Flow<List<Goal>> = allGoalsRaw().map { goals ->
        goals.sortedWith(compareBy<Goal> { it.priority }.thenByDescending { it.createdAt })
    }

    suspend fun getTopGoal(): Goal? = activeGoals.first().minByOrNull { it.priority }

    suspend fun addGoal(goal: Goal): Long {
        val uid = authRepository.currentUserId ?: return 0L
        val id = if (goal.id != 0L) goal.id else System.currentTimeMillis()
        try {
            goalsCollection(uid).document(id.toString()).set(goal.copy(id = id).toFirestoreMap()).await()
        } catch (_: Exception) {
            // Write failed (offline, denied, etc.) — the UI reacts to the goals
            // Flow, so there's nothing else to reconcile here; just don't crash.
        }
        return id
    }

    suspend fun updateGoal(goal: Goal) {
        val uid = authRepository.currentUserId ?: return
        try {
            goalsCollection(uid).document(goal.id.toString()).set(goal.toFirestoreMap()).await()
        } catch (_: Exception) {
        }
    }

    suspend fun deleteGoal(goal: Goal) {
        val uid = authRepository.currentUserId ?: return
        try {
            goalsCollection(uid).document(goal.id.toString()).delete().await()
        } catch (_: Exception) {
        }
    }
}

private fun Goal.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "description" to description,
    "priority" to priority,
    "estimatedMinutes" to estimatedMinutes,
    "completedMinutes" to completedMinutes,
    "completed" to completed,
    "createdAt" to createdAt
)

private fun DocumentSnapshot.toGoal(): Goal? {
    val data = data ?: return null
    return Goal(
        id = (data["id"] as? Long) ?: id.toLongOrNull() ?: return null,
        title = data["title"] as? String ?: return null,
        description = data["description"] as? String ?: "",
        priority = (data["priority"] as? Long)?.toInt() ?: 0,
        estimatedMinutes = (data["estimatedMinutes"] as? Long)?.toInt() ?: 0,
        completedMinutes = (data["completedMinutes"] as? Long)?.toInt() ?: 0,
        completed = data["completed"] as? Boolean ?: false,
        createdAt = (data["createdAt"] as? Long) ?: 0L
    )
}
