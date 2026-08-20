package com.focusloop.app.domain.model

data class Goal(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: Int = 0,
    val estimatedMinutes: Int = 0,
    val completedMinutes: Int = 0,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercent: Int
        get() = if (estimatedMinutes > 0) {
            ((completedMinutes.toFloat() / estimatedMinutes) * 100).toInt().coerceIn(0, 100)
        } else 0
}
