package com.focusloop.app.domain.model

data class UserSettings(
    val interventionThresholdMs: Long = 5 * 60 * 1000L, // 5 minutes default
    val cooldownDurationMs: Long = 15 * 60 * 1000L,      // 15 minutes cooldown
    val monitoredPackages: Set<String> = emptySet(),
    val monitoringEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val developerModeEnabled: Boolean = false,
    val demoThresholdMs: Long = 10 * 1000L,              // 10 seconds for demo
    val hobbies: Set<String> = emptySet(),
    val todos: List<TodoItem> = emptyList()
)

// Predefined distracting apps with known package names
object KnownDistractingApps {
    val defaults = listOf(
        DistractingApp("com.zhiliaoapp.musically", "TikTok", "🎵"),
        DistractingApp("com.instagram.android", "Instagram", "📸"),
        DistractingApp("com.google.android.youtube", "YouTube", "▶️"),
        DistractingApp("com.reddit.frontpage", "Reddit", "🤖"),
        DistractingApp("com.twitter.android", "X (Twitter)", "🐦"),
        DistractingApp("com.facebook.katana", "Facebook", "👥"),
        DistractingApp("com.snapchat.android", "Snapchat", "👻"),
        DistractingApp("com.android.chrome", "Chrome", "🌐"),
        DistractingApp("com.linkedin.android", "LinkedIn", "💼"),
        DistractingApp("com.pinterest", "Pinterest", "📌")
    )
}

data class DistractingApp(
    val packageName: String,
    val displayName: String,
    val emoji: String,
    val isInstalled: Boolean = false
)
