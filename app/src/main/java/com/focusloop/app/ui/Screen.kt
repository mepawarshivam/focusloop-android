package com.focusloop.app.ui

sealed class Screen(val route: String) {
    // Resolves auth + onboarding state before routing anywhere else
    object Splash : Screen("splash")

    // Auth
    object Auth : Screen("auth")

    // Onboarding
    object Onboarding : Screen("onboarding")
    object OnboardingWelcome : Screen("onboarding_welcome")
    object OnboardingGoals : Screen("onboarding_goals")
    object OnboardingApps : Screen("onboarding_apps")
    object OnboardingTiming : Screen("onboarding_timing")
    object OnboardingPermissions : Screen("onboarding_permissions")

    // Main app
    object Home : Screen("home")
    object Insights : Screen("insights")
    object Challenges : Screen("challenges")
    object Settings : Screen("settings")

    // Sub-screens
    object FocusSession : Screen("focus_session/{goalId}/{goalTitle}") {
        fun createRoute(goalId: Long, goalTitle: String) = "focus_session/$goalId/$goalTitle"
    }
    object Intervention : Screen("intervention")
    object Chat : Screen("chat")
    object AddGoal : Screen("add_goal")
    object EditGoal : Screen("edit_goal/{goalId}") {
        fun createRoute(goalId: Long) = "edit_goal/$goalId"
    }
    object EditHobbies : Screen("edit_hobbies")
    object EditApps : Screen("edit_apps")
    object Privacy : Screen("privacy")
    object About : Screen("about")
}
