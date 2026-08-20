package com.focusloop.app.domain.model

data class LearningQuestion(
    val id: Long = 0,
    val category: QuestionCategory,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int, // Index into options
    val explanation: String,
    val difficulty: Difficulty = Difficulty.MEDIUM
)

enum class QuestionCategory(val displayName: String) {
    DSA("Data Structures & Algorithms"),
    SYSTEM_DESIGN("System Design"),
    JAVASCRIPT("JavaScript"),
    PROGRAMMING("Programming"),
    GENERAL("General Knowledge"),
    MATH("Mathematics"),
    PRODUCTIVITY("Productivity")
}

enum class Difficulty { EASY, MEDIUM, HARD }

data class LearningAttempt(
    val id: Long = 0,
    val questionId: Long,
    val answeredCorrectly: Boolean,
    val attemptedAt: Long = System.currentTimeMillis(),
    val xpEarned: Int
)

data class UserProgress(
    val focusXp: Int = 0,
    val learningXp: Int = 0,
    val currentStreak: Int = 0,
    val totalMinutesRecovered: Int = 0,
    val totalFocusSessions: Int = 0,
    val totalInterventions: Int = 0
) {
    val totalXp: Int get() = focusXp + learningXp

    val level: Int get() = when {
        totalXp < 100 -> 1
        totalXp < 300 -> 2
        totalXp < 600 -> 3
        totalXp < 1000 -> 4
        totalXp < 1500 -> 5
        totalXp < 2500 -> 6
        else -> 7
    }

    val levelTitle: String get() = when (level) {
        1 -> "Beginner"
        2 -> "Attention Rookie"
        3 -> "Focus Apprentice"
        4 -> "Focus Builder"
        5 -> "Deep Worker"
        6 -> "Attention Master"
        else -> "Focus Guardian"
    }

    val focusScore: Int get() {
        // Simple score 0-100 based on recent behavior
        val sessionScore = (totalFocusSessions * 10).coerceAtMost(40)
        val recoveryScore = (totalMinutesRecovered / 2).coerceAtMost(40)
        val streakScore = (currentStreak * 3).coerceAtMost(20)
        return (sessionScore + recoveryScore + streakScore).coerceIn(0, 100)
    }
}
