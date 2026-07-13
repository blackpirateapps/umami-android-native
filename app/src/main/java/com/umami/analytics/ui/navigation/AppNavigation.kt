package com.umami.analytics.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.umami.analytics.data.api.UmamiApiService
import com.umami.analytics.data.api.UmamiRepository
import com.umami.analytics.data.api.models.WebsiteDto
import com.umami.analytics.data.db.UmamiDatabase
import com.umami.analytics.data.preferences.SessionManager
import com.umami.analytics.ui.components.AppSidebar
import com.umami.analytics.ui.screens.login.LoginScreen
import com.umami.analytics.ui.screens.login.LoginViewModel
import com.umami.analytics.ui.screens.overview.OverviewScreen
import com.umami.analytics.ui.screens.overview.OverviewViewModel
import com.umami.analytics.ui.screens.sessions.SessionsScreen
import com.umami.analytics.ui.screens.sessions.SessionsViewModel
import com.umami.analytics.ui.screens.settings.SettingsScreen
import com.umami.analytics.ui.screens.websites.WebsiteDetailScreen
import com.umami.analytics.ui.screens.websites.WebsitesScreen
import com.umami.analytics.util.NetworkObserver
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Overview : Screen("overview")
    object Sessions : Screen("sessions")
    object Websites : Screen("websites")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    apiService: UmamiApiService,
    database: UmamiDatabase,
    sessionManager: SessionManager,
    networkObserver: NetworkObserver,
    onThemeModeChange: (String) -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val repository = remember {
        UmamiRepository(apiService, database, sessionManager, networkObserver)
    }

    val startDestination = if (sessionManager.isLoggedIn()) Screen.Overview.route else Screen.Login.route

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Overview.route

    val overviewViewModel = remember {
        OverviewViewModel(repository, sessionManager, networkObserver)
    }
    val sessionsViewModel = remember {
        SessionsViewModel(repository, sessionManager, networkObserver)
    }
    val loginViewModel = remember {
        LoginViewModel(apiService, sessionManager)
    }

    val overviewUiState by overviewViewModel.uiState.collectAsState()
    var selectedWebsiteDetail by remember { mutableStateOf<WebsiteDto?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute != Screen.Login.route,
        drawerContent = {
            AppSidebar(
                websites = overviewUiState.websites,
                selectedWebsiteId = overviewUiState.selectedWebsiteId,
                currentRoute = currentRoute,
                onWebsiteSelected = { site ->
                    scope.launch { drawerState.close() }
                    overviewViewModel.selectWebsite(site)
                    sessionsViewModel.loadWebsitesAndSessions()
                },
                onNavigateToOverview = {
                    scope.launch { drawerState.close() }
                    if (currentRoute != Screen.Overview.route) {
                        navController.navigate(Screen.Overview.route) {
                            popUpTo(Screen.Overview.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onNavigateToSessions = {
                    scope.launch { drawerState.close() }
                    if (currentRoute != Screen.Sessions.route) {
                        navController.navigate(Screen.Sessions.route) {
                            popUpTo(Screen.Overview.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onNavigateToWebsites = {
                    scope.launch { drawerState.close() }
                    if (currentRoute != Screen.Websites.route) {
                        navController.navigate(Screen.Websites.route) {
                            popUpTo(Screen.Overview.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onNavigateToSettings = {
                    scope.launch { drawerState.close() }
                    if (currentRoute != Screen.Settings.route) {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Overview.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    sessionManager.clearSession()
                    scope.launch { database.clearAllTables() }
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 300 },
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(280))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -300 },
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(280))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -300 },
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(280))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 300 },
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(280))
            }
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        overviewViewModel.loadWebsites()
                        sessionsViewModel.loadWebsitesAndSessions()
                        navController.navigate(Screen.Overview.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Overview.route) {
                OverviewScreen(
                    viewModel = overviewViewModel,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }

            composable(Screen.Sessions.route) {
                SessionsScreen(
                    viewModel = sessionsViewModel,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }

            composable(Screen.Websites.route) {
                WebsitesScreen(
                    websites = overviewUiState.websites,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onSelectWebsiteDetail = { site ->
                        selectedWebsiteDetail = site
                        navController.navigate("website_detail")
                    }
                )
            }

            composable("website_detail") {
                selectedWebsiteDetail?.let { site ->
                    WebsiteDetailScreen(
                        website = site,
                        serverUrl = sessionManager.getServerUrl() ?: "https://analytics.umami.is",
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    sessionManager = sessionManager,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onThemeModeChange = onThemeModeChange,
                    onClearCache = {
                        scope.launch {
                            database.clearAllTables()
                            overviewViewModel.refreshData()
                            sessionsViewModel.refreshData()
                        }
                    }
                )
            }
        }
    }
}
