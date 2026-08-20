package com.focusloop.app.domain.model

data class DistractionSession(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val startedAt: Long,
    val endedAt: Long = 0,
    val durationMs: Long = 0,
    val interventionTriggered: Boolean = false
)

data class FocusSession(
    val id: Long = 0,
    val goalId: Long,
    val goalTitle: String,
    val startedAt: Long,
    val endedAt: Long = 0,
    val durationMinutes: Int = 0,
    val completed: Boolean = false
)

data class DailyStats(
    val id: Long = 0,
    val date: Long, // Start of day timestamp
    val totalDistractionMs: Long = 0,
    val totalFocusMs: Long = 0,
    val interventionCount: Int = 0,
    val sessionsCompleted: Int = 0,
    val focusXpEarned: Int = 0,
    val learningXpEarned: Int = 0
)
