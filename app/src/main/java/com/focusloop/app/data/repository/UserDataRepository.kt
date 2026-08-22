package com.focusloop.app.data.repository

import com.focusloop.app.domain.model.TodoItem
import com.focusloop.app.domain.model.UserSettings
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val USERS_COLLECTION = "users"

/**
 * All account-bound app data — settings, hobbies, to-dos, and gamification
 * counters — stored in a single Firestore document per user at
 * users/{uid}. Reads are reactive (Firestore snapshot listeners); writes go
 * straight to the server and Firestore's own offline cache handles the
 * no-connectivity case transparently.
 */
class UserDataRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val authRepository: AuthRepository
) {

    private fun userDoc(uid: String) = firestore.collection(USERS_COLLECTION).document(uid)

    /**
     * Writes are fire-and-forget from the UI's perspective — screens react to
     * [settings] rather than a write's return value, so a failure (offline,
     * denied, etc.) has nothing to roll back. Swallow it instead of crashing.
     */
    private suspend inline fun safeWrite(block: suspend () -> Unit) {
        try {
            block()
        } catch (_: Exception) {
        }
    }

    /** Emits the current user's document data, or null when logged out / doc doesn't exist yet. */
    private fun documentDataFlow(): Flow<Map<String, Any?>?> =
        authRepository.authState
            .map { it.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid == null) {
                    flowOf(null)
                } else {
                    callbackFlow {
                        val registration = userDoc(uid).addSnapshotListener { snapshot, _ ->
                            trySend(snapshot?.data)
                        }
                        awaitClose { registration.remove() }
                    }
                }
            }

    val settings: Flow<UserSettings> = documentDataFlow().map { it.toUserSettings() }

    suspend fun updateSettings(settings: UserSettings) = safeWrite {
        val uid = authRepository.currentUserId ?: return@safeWrite
        userDoc(uid).set(settings.toFirestoreMap(), SetOptions.merge()).await()
    }

    suspend fun setHobbies(hobbies: Set<String>) = safeWrite {
        val uid = authRepository.currentUserId ?: return@safeWrite
        userDoc(uid).set(mapOf("hobbies" to hobbies.toList()), SetOptions.merge()).await()
    }

    suspend fun setOnboardingCompleted() = safeWrite {
        val uid = authRepository.currentUserId ?: return@safeWrite
        userDoc(uid).set(mapOf("onboardingCompleted" to true), SetOptions.merge()).await()
    }

    suspend fun setMonitoringEnabled(enabled: Boolean) = safeWrite {
        val uid = authRepository.currentUserId ?: return@safeWrite
        userDoc(uid).set(mapOf("monitoringEnabled" to enabled), SetOptions.merge()).await()
    }

    suspend fun addTodo(text: String) = safeWrite {
        val uid = authRepository.currentUserId ?: return@safeWrite
        val newTodo = TodoItem(id = UUID.randomUUID().toString(), text = text)
        firestore.runTransaction { tx ->
            val snapshot = tx.get(userDoc(uid))
            val current = snapshot.data.toUserSettings().todos
            tx.set(userDoc(uid), mapOf("todos" to (current + newTodo).toFirestoreList()), SetOptions.merge())
        }.await()
    }

    suspend fun toggleTodo(id: String) = safeWrite {
        val uid = authRepository.currentUserId ?: return@safeWrite
        firestore.runTransaction { tx ->
            val snapshot = tx.get(userDoc(uid))
            val current = snapshot.data.toUserSettings().todos
            val updated = current.map { if (it.id == id) it.copy(completed = !it.completed) else it }
            tx.set(userDoc(uid), mapOf("todos" to updated.toFirestoreList()), SetOptions.merge())
        }.await()
    }

    suspend fun removeTodo(id: String) = safeWrite {
        val uid = authRepository.currentUserId ?: return@safeWrite
        firestore.runTransaction { tx ->
            val snapshot = tx.get(userDoc(uid))
            val current = snapshot.data.toUserSettings().todos
            tx.set(userDoc(uid), mapOf("todos" to current.filterNot { it.id == id }.toFirestoreList()), SetOptions.merge())
        }.await()
    }

    // Gamification counters — all fields on the same per-user document.
    val focusXp: Flow<Int> = documentDataFlow().map { (it?.get("focusXp") as? Long)?.toInt() ?: 0 }
    val learningXp: Flow<Int> = documentDataFlow().map { (it?.get("learningXp") as? Long)?.toInt() ?: 0 }
    val currentStreak: Flow<Int> = documentDataFlow().map { (it?.get("currentStreak") as? Long)?.toInt() ?: 0 }
    val totalMinutesRecovered: Flow<Int> = documentDataFlow().map { (it?.get("totalMinutesRecovered") as? Long)?.toInt() ?: 0 }
    val totalFocusSessions: Flow<Int> = documentDataFlow().map { (it?.get("totalFocusSessions") as? Long)?.toInt() ?: 0 }
    val totalInterventions: Flow<Int> = documentDataFlow().map { (it?.get("totalInterventions") as? Long)?.toInt() ?: 0 }

    suspend fun addFocusXp(amount: Int) = safeWrite {
        val uid = authRepository.currentUserId ?: return@safeWrite
        userDoc(uid).set(mapOf("focusXp" to FieldValue.increment(amount.toLong())), SetOptions.merge()).await()
    }

    suspend fun addLearningXp(amount: Int) = safeWrite {
        val uid = authRepository.currentUserId ?: return@safeWrite
        userDoc(uid).set(mapOf("learningXp" to FieldValue.increment(amount.toLong())), SetOptions.merge()).await()
    }

    suspend fun addRecoveredMinutes(minutes: Int) = safeWrite {
        val uid = authRepository.currentUserId ?: return@safeWrite
        userDoc(uid).set(
            mapOf(
                "totalMinutesRecovered" to FieldValue.increment(minutes.toLong()),
                "totalFocusSessions" to FieldValue.increment(1)
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun recordIntervention() = safeWrite {
        val uid = authRepository.currentUserId ?: return@safeWrite
        userDoc(uid).set(mapOf("totalInterventions" to FieldValue.increment(1)), SetOptions.merge()).await()
    }

    suspend fun updateStreak(today: Long) = safeWrite {
        val uid = authRepository.currentUserId ?: return@safeWrite
        firestore.runTransaction { tx ->
            val snapshot = tx.get(userDoc(uid))
            val lastDate = (snapshot.getLong("lastStreakDate")) ?: 0L
            val dayMs = 24 * 60 * 60 * 1000L
            val currentStreak = (snapshot.getLong("currentStreak"))?.toInt() ?: 0
            val newStreak = if (today - lastDate < dayMs * 2) currentStreak + 1 else 1
            tx.set(
                userDoc(uid),
                mapOf("currentStreak" to newStreak, "lastStreakDate" to today),
                SetOptions.merge()
            )
        }.await()
    }
}

private fun UserSettings.toFirestoreMap(): Map<String, Any?> = mapOf(
    "interventionThresholdMs" to interventionThresholdMs,
    "cooldownDurationMs" to cooldownDurationMs,
    "monitoredPackages" to monitoredPackages.toList(),
    "monitoringEnabled" to monitoringEnabled,
    "onboardingCompleted" to onboardingCompleted,
    "notificationsEnabled" to notificationsEnabled,
    "vibrationEnabled" to vibrationEnabled,
    "soundEnabled" to soundEnabled,
    "darkModeEnabled" to darkModeEnabled,
    "developerModeEnabled" to developerModeEnabled,
    "demoThresholdMs" to demoThresholdMs,
    "hobbies" to hobbies.toList(),
    "todos" to todos.toFirestoreList()
)

private fun List<TodoItem>.toFirestoreList(): List<Map<String, Any?>> =
    map { mapOf("id" to it.id, "text" to it.text, "completed" to it.completed) }

private fun Map<String, Any?>?.toUserSettings(): UserSettings {
    if (this == null) return UserSettings()
    val monitoredPackages = (get("monitoredPackages") as? List<*>)
        ?.mapNotNull { it as? String }?.toSet() ?: emptySet()
    val hobbies = (get("hobbies") as? List<*>)
        ?.mapNotNull { it as? String }?.toSet() ?: emptySet()
    val todos = (get("todos") as? List<*>)?.mapNotNull { raw ->
        val map = raw as? Map<*, *> ?: return@mapNotNull null
        val id = map["id"] as? String ?: return@mapNotNull null
        TodoItem(
            id = id,
            text = map["text"] as? String ?: "",
            completed = map["completed"] as? Boolean ?: false
        )
    } ?: emptyList()

    return UserSettings(
        interventionThresholdMs = (get("interventionThresholdMs") as? Long) ?: (5 * 60 * 1000L),
        cooldownDurationMs = (get("cooldownDurationMs") as? Long) ?: (15 * 60 * 1000L),
        monitoredPackages = monitoredPackages,
        monitoringEnabled = (get("monitoringEnabled") as? Boolean) ?: false,
        onboardingCompleted = (get("onboardingCompleted") as? Boolean) ?: false,
        notificationsEnabled = (get("notificationsEnabled") as? Boolean) ?: true,
        vibrationEnabled = (get("vibrationEnabled") as? Boolean) ?: true,
        soundEnabled = (get("soundEnabled") as? Boolean) ?: false,
        darkModeEnabled = (get("darkModeEnabled") as? Boolean) ?: false,
        developerModeEnabled = (get("developerModeEnabled") as? Boolean) ?: false,
        demoThresholdMs = (get("demoThresholdMs") as? Long) ?: (10 * 1000L),
        hobbies = hobbies,
        todos = todos
    )
}

private fun DocumentSnapshot.toUserSettings(): UserSettings = data.toUserSettings()
