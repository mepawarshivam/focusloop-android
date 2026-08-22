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
        DistractingApp("com.zhiliaoapp.musically", "TikTok", 0xFF010101),
        DistractingApp("com.instagram.android", "Instagram", 0xFFE1306C),
        DistractingApp("com.google.android.youtube", "YouTube", 0xFFFF0000),
        DistractingApp("com.reddit.frontpage", "Reddit", 0xFFFF4500),
        DistractingApp("com.twitter.android", "X (Twitter)", 0xFF14171A),
        DistractingApp("com.facebook.katana", "Facebook", 0xFF1877F2),
        DistractingApp("com.snapchat.android", "Snapchat", 0xFFF7B500),
        DistractingApp("com.android.chrome", "Chrome", 0xFF4285F4),
        DistractingApp("com.linkedin.android", "LinkedIn", 0xFF0A66C2),
        DistractingApp("com.pinterest", "Pinterest", 0xFFE60023)
    )
}

data class DistractingApp(
    val packageName: String,
    val displayName: String,
    val colorArgb: Long,
    val isInstalled: Boolean = false
)
