package com.focusloop.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: Int = 0,
    val estimatedMinutes: Int = 0,
    val completedMinutes: Int = 0,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "distraction_sessions")
data class DistractionSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val startedAt: Long,
    val endedAt: Long = 0,
    val durationMs: Long = 0,
    val interventionTriggered: Boolean = false
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val goalTitle: String,
    val startedAt: Long,
    val endedAt: Long = 0,
    val durationMinutes: Int = 0,
    val completed: Boolean = false
)

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val totalDistractionMs: Long = 0,
    val totalFocusMs: Long = 0,
    val interventionCount: Int = 0,
    val sessionsCompleted: Int = 0,
    val focusXpEarned: Int = 0,
    val learningXpEarned: Int = 0
)

@Entity(tableName = "learning_questions")
data class LearningQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val question: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val correctAnswer: Int,
    val explanation: String,
    val difficulty: String = "MEDIUM"
)

@Entity(tableName = "learning_attempts")
data class LearningAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val answeredCorrectly: Boolean,
    val attemptedAt: Long = System.currentTimeMillis(),
    val xpEarned: Int
)
