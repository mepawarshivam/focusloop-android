package com.focusloop.app.data.repository

import com.focusloop.app.data.local.dao.GoalDao
import com.focusloop.app.data.local.entity.GoalEntity
import com.focusloop.app.domain.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepository(private val dao: GoalDao) {

    val activeGoals: Flow<List<Goal>> = dao.getActiveGoals().map { list ->
        list.map { it.toDomain() }
    }

    val allGoals: Flow<List<Goal>> = dao.getAllGoals().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getTopGoal(): Goal? = dao.getTopGoal()?.toDomain()

    suspend fun addGoal(goal: Goal): Long = dao.insert(goal.toEntity())

    suspend fun updateGoal(goal: Goal) = dao.update(goal.toEntity())

    suspend fun deleteGoal(goal: Goal) = dao.delete(goal.toEntity())

    suspend fun completeGoal(goalId: Long) {
        val entity = dao.getTopGoal() ?: return
        dao.update(entity.copy(completed = true))
    }

    suspend fun addProgressMinutes(goalId: Long, minutes: Int) {
        // Get goal and update completedMinutes
        dao.getTopGoal()?.let { entity ->
            if (entity.id == goalId) {
                dao.update(entity.copy(completedMinutes = entity.completedMinutes + minutes))
            }
        }
    }

    private fun GoalEntity.toDomain() = Goal(
        id = id,
        title = title,
        description = description,
        priority = priority,
        estimatedMinutes = estimatedMinutes,
        completedMinutes = completedMinutes,
        completed = completed,
        createdAt = createdAt
    )

    private fun Goal.toEntity() = GoalEntity(
        id = id,
        title = title,
        description = description,
        priority = priority,
        estimatedMinutes = estimatedMinutes,
        completedMinutes = completedMinutes,
        completed = completed,
        createdAt = createdAt
    )
}
