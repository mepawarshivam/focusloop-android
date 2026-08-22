package com.focusloop.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.focusloop.app.data.repository.UserDataRepository
import com.focusloop.app.service.FocusMonitoringService
import com.focusloop.app.ui.Screen
import com.focusloop.app.ui.auth.AuthScreen
import com.focusloop.app.ui.auth.AuthViewModel
import com.focusloop.app.ui.challenges.ChallengesScreen
import com.focusloop.app.ui.challenges.ChallengesViewModel
import com.focusloop.app.ui.chat.ChatScreen
import com.focusloop.app.ui.chat.ChatViewModel
import com.focusloop.app.ui.focussession.FocusSessionScreen
import com.focusloop.app.ui.focussession.FocusSessionViewModel
import com.focusloop.app.ui.home.*
import com.focusloop.app.ui.insights.InsightsScreen
import com.focusloop.app.ui.insights.InsightsViewModel
import com.focusloop.app.ui.onboarding.OnboardingScreen
import com.focusloop.app.ui.onboarding.OnboardingViewModel
import com.focusloop.app.ui.settings.EditAppsScreen
import com.focusloop.app.ui.settings.EditAppsViewModel
import com.focusloop.app.ui.settings.EditHobbiesScreen
import com.focusloop.app.ui.settings.EditHobbiesViewModel
import com.focusloop.app.ui.settings.SettingsScreen
import com.focusloop.app.ui.settings.SettingsViewModel
import com.focusloop.app.ui.theme.FocusLoopTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import compose.icons.FeatherIcons
import compose.icons.feathericons.Award
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.Home
import compose.icons.feathericons.Settings

class MainActivity : ComponentActivity() {

    private val app get() = application as FocusLoopApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        // Auth + settings now live in Firebase, so resolving the start destination
        // means a network-backed read — never block onCreate on it. FocusLoopNavHost
        // starts on a Splash route and navigates on once that resolves.
        setContent {
            val settingsFlow = app.settingsDataStore.settings.collectAsState(
                initial = com.focusloop.app.domain.model.UserSettings()
            )
            val darkMode = settingsFlow.value.darkModeEnabled

            FocusLoopTheme(darkTheme = darkMode) {
                FocusLoopNavHost(
                    app = app,
                    startDestination = Screen.Splash.route,
                    onStartService = ::startMonitoringService,
                    onStopService = ::stopMonitoringService
                )
            }
        }
    }

    private fun startMonitoringService() {
        val intent = Intent(this, FocusMonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopMonitoringService() {
        stopService(Intent(this, FocusMonitoringService::class.java))
    }
}

@Composable
fun FocusLoopNavHost(
    app: FocusLoopApplication,
    startDestination: String,
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(200)) }
    ) {
        // Resolves auth + onboarding state (both now network-backed) before routing
        composable(Screen.Splash.route) {
            LaunchedEffect(Unit) {
                val isLoggedIn = app.authDataStore.authState.first().isLoggedIn
                val destination = if (isLoggedIn) {
                    val onboardingDone = app.settingsDataStore.settings.first().onboardingCompleted
                    if (onboardingDone) Screen.Home.route else Screen.Onboarding.route
                } else {
                    Screen.Auth.route
                }
                navController.navigate(destination) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Auth gate
        composable(Screen.Auth.route) {
            val vm = remember { AuthViewModel(app.authDataStore) }
            val scope = rememberCoroutineScope()
            AuthScreen(viewModel = vm, onAuthenticated = {
                scope.launch {
                    val onboardingDone = app.settingsDataStore.settings.first().onboardingCompleted
                    val destination = if (onboardingDone) Screen.Home.route else Screen.Onboarding.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            })
        }

        // Onboarding
        composable(Screen.Onboarding.route) {
            val vm = remember {
                OnboardingViewModel(app.settingsDataStore, app.goalRepository)
            }
            OnboardingScreen(viewModel = vm, onComplete = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        // Main app with bottom nav
        composable(Screen.Home.route) {
            MainAppScaffold(
                app = app,
                currentRoute = Screen.Home.route,
                navController = navController,
                onStartService = onStartService,
                onStopService = onStopService
            )
        }

        // Focus session
        composable(
            route = Screen.FocusSession.route,
            arguments = listOf(
                androidx.navigation.navArgument("goalId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("goalTitle") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStack ->
            val goalId = backStack.arguments?.getLong("goalId") ?: -1L
            val goalTitle = backStack.arguments?.getString("goalTitle") ?: ""
            val vm = remember {
                FocusSessionViewModel(app.settingsDataStore, app.sessionRepository, goalId, goalTitle)
            }
            FocusSessionScreen(viewModel = vm, onDone = { navController.popBackStack() })
        }

        // Add goal
        composable(Screen.AddGoal.route) {
            val vm = remember { AddGoalViewModel(app.goalRepository) }
            AddGoalScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        // AI coach chat
        composable(Screen.Chat.route) {
            val vm = remember { ChatViewModel(app.chatRepository, app.settingsDataStore, app.goalRepository) }
            ChatScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        // Edit hobbies / interests
        composable(Screen.EditHobbies.route) {
            val vm = remember { EditHobbiesViewModel(app.settingsDataStore) }
            EditHobbiesScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        // Edit monitored apps
        composable(Screen.EditApps.route) {
            val vm = remember { EditAppsViewModel(app.settingsDataStore) }
            EditAppsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem("Home", Screen.Home.route, FeatherIcons.Home),
    BottomNavItem("Insights", "insights", FeatherIcons.BarChart2),
    BottomNavItem("Challenges", "challenges", FeatherIcons.Award),
    BottomNavItem("Settings", "settings", FeatherIcons.Settings)
)

@Composable
fun MainAppScaffold(
    app: FocusLoopApplication,
    currentRoute: String,
    navController: androidx.navigation.NavHostController,
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Observe monitoring state to start/stop service
    val settings by app.settingsDataStore.settings.collectAsState(
        initial = com.focusloop.app.domain.model.UserSettings()
    )
    LaunchedEffect(settings.monitoringEnabled) {
        if (settings.monitoringEnabled) onStartService() else onStopService()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            innerNavController.navigate(item.route) {
                                popUpTo(innerNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(item.icon, item.label)
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val vm = remember { HomeViewModel(app.settingsDataStore, app.goalRepository, app.sessionRepository) }
                HomeScreen(
                    viewModel = vm,
                    onStartFocusSession = { id, title ->
                        navController.navigate(Screen.FocusSession.createRoute(id, title))
                    },
                    onAddGoal = { navController.navigate(Screen.AddGoal.route) },
                    onOpenChat = { navController.navigate(Screen.Chat.route) }
                )
            }
            composable("insights") {
                val vm = remember { InsightsViewModel(app.sessionRepository) }
                InsightsScreen(viewModel = vm)
            }
            composable("challenges") {
                val vm = remember { ChallengesViewModel(app.learningRepository, app.settingsDataStore) }
                ChallengesScreen(viewModel = vm)
            }
            composable("settings") {
                val vm = remember { SettingsViewModel(app.settingsDataStore, app.authDataStore) }
                SettingsScreen(
                    viewModel = vm,
                    onLoggedOut = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onEditHobbies = { navController.navigate(Screen.EditHobbies.route) },
                    onEditApps = { navController.navigate(Screen.EditApps.route) }
                )
            }
        }
    }
}
