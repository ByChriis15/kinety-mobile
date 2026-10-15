package com.apps.kinety_mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apps.kinety_mobile.data.repository.KinetyRepository
import com.apps.kinety_mobile.ui.components.KinetyBottomNav
import com.apps.kinety_mobile.ui.navigation.Screen
import com.apps.kinety_mobile.ui.screens.auth.AuthScreen
import com.apps.kinety_mobile.ui.screens.feed.FeedScreen
import com.apps.kinety_mobile.ui.screens.issues.IssueDetailScreen
import com.apps.kinety_mobile.ui.screens.issues.UpsertIssueScreen
import com.apps.kinety_mobile.ui.screens.projects.ProjectDetailScreen
import com.apps.kinety_mobile.ui.screens.projects.ProjectsScreen
import com.apps.kinety_mobile.ui.screens.settings.SettingsScreen
import com.apps.kinety_mobile.ui.screens.splash.SplashScreen
import com.apps.kinety_mobile.ui.theme.AppThemeMode
import com.apps.kinety_mobile.ui.theme.KinetyTheme

@Composable
fun KinetyApp() {
    val context = LocalContext.current
    val repository = remember { KinetyRepository(context) }
    val themeMode by repository.sessionManager.themeModeFlow.collectAsState(initial = AppThemeMode.MERLOT_NOIR)

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Feed.route,
        Screen.Projects.route,
        Screen.Settings.route
    )

    KinetyTheme(themeMode = themeMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.fillMaxSize()
            ) {
                // Splash Screen
                composable(Screen.Splash.route) {
                    SplashScreen(
                        sessionManager = repository.sessionManager,
                        onNavigateToAuth = {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        onNavigateToFeed = {
                            navController.navigate(Screen.Feed.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Auth Screen
                composable(Screen.Auth.route) {
                    AuthScreen(
                        repository = repository,
                        onAuthSuccess = {
                            navController.navigate(Screen.Feed.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Feed Screen (Mis Issues)
                composable(Screen.Feed.route) {
                    FeedScreen(
                        repository = repository,
                        onIssueClick = { issueId ->
                            navController.navigate(Screen.IssueDetail.createRoute(issueId))
                        },
                        onCreateIssueClick = {
                            navController.navigate(Screen.UpsertIssue.createRoute())
                        }
                    )
                }

                // Projects Screen
                composable(Screen.Projects.route) {
                    ProjectsScreen(
                        repository = repository,
                        onProjectClick = { projectId ->
                            navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                        }
                    )
                }

                // Project Detail Screen
                composable(
                    route = Screen.ProjectDetail.route,
                    arguments = listOf(navArgument("projectId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    ProjectDetailScreen(
                        projectId = projectId,
                        repository = repository,
                        onBack = { navController.popBackStack() },
                        onIssueClick = { issueId ->
                            navController.navigate(Screen.IssueDetail.createRoute(issueId))
                        },
                        onCreateIssueClick = { projId ->
                            navController.navigate(Screen.UpsertIssue.createRoute(projectId = projId))
                        }
                    )
                }

                // Issue Detail Screen
                composable(
                    route = Screen.IssueDetail.route,
                    arguments = listOf(navArgument("issueId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val issueId = backStackEntry.arguments?.getString("issueId") ?: ""
                    IssueDetailScreen(
                        issueId = issueId,
                        repository = repository,
                        onBack = { navController.popBackStack() },
                        onEdit = { id ->
                            navController.navigate(Screen.UpsertIssue.createRoute(issueId = id))
                        }
                    )
                }

                // Upsert Issue Screen (Create / Edit)
                composable(
                    route = Screen.UpsertIssue.route,
                    arguments = listOf(
                        navArgument("issueId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument("projectId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val issueId = backStackEntry.arguments?.getString("issueId")
                    val projectId = backStackEntry.arguments?.getString("projectId")
                    UpsertIssueScreen(
                        issueId = if (issueId.isNullOrBlank()) null else issueId,
                        initialProjectId = if (projectId.isNullOrBlank()) null else projectId,
                        repository = repository,
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() }
                    )
                }

                // Settings Screen
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        repository = repository,
                        onLogout = {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Floating Capsule Bottom Navigation Bar
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                KinetyBottomNav(
                    currentRoute = currentRoute ?: Screen.Feed.route,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(Screen.Feed.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}
