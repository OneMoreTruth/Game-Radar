package com.example.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.CollectionsScreen
import com.example.ui.screens.GameDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    data object Home : Screen("home", "Discover", Icons.Default.SportsEsports, Icons.Outlined.SportsEsports)
    data object Search : Screen("search", "Search", Icons.Default.Search, Icons.Outlined.Search)
    data object Wishlist : Screen("wishlist", "Wishlist", Icons.Default.Favorite, Icons.Outlined.FavoriteBorder)
    data object Collections : Screen("collections", "Library", Icons.Default.CollectionsBookmark, Icons.Outlined.CollectionsBookmark)
    data object Alerts : Screen("alerts", "Alerts", Icons.Default.Notifications, Icons.Outlined.Notifications)
    data object GameDetail : Screen("detail/{gameId}", "Details", Icons.Default.Info, Icons.Outlined.Info) {
        fun createRoute(gameId: String) = "detail/$gameId"
    }
}

val BOTTOM_NAV_ITEMS = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Wishlist,
    Screen.Collections,
    Screen.Alerts
)

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = currentRoute in BOTTOM_NAV_ITEMS.map { it.route }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = GamerSurface,
                    contentColor = GamerTextPrimary,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = GamerBorder,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .testTag("bottom_navigation_bar")
                ) {
                    val alerts by viewModel.alertHistory.collectAsState()
                    val wishlist by viewModel.wishlistItems.collectAsState()

                    BOTTOM_NAV_ITEMS.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (screen == Screen.Alerts && alerts.isNotEmpty()) {
                                            Badge(containerColor = NeonPink) {
                                                Text("${alerts.size}")
                                            }
                                        } else if (screen == Screen.Wishlist && wishlist.isNotEmpty()) {
                                            Badge(containerColor = NeonGreen) {
                                                Text("${wishlist.size}")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title,
                                        tint = if (selected) NeonCyan else GamerTextSecondary
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    color = if (selected) NeonCyan else GamerTextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                unselectedIconColor = GamerTextSecondary,
                                selectedTextColor = NeonCyan,
                                unselectedTextColor = GamerTextSecondary,
                                indicatorColor = GamerSurfaceHighlight
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        },
        containerColor = GamerBg,
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = if (shouldShowBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { gameId ->
                        navController.navigate(Screen.GameDetail.createRoute(gameId))
                    },
                    onNavigateToAlerts = {
                        navController.navigate(Screen.Alerts.route)
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { gameId ->
                        navController.navigate(Screen.GameDetail.createRoute(gameId))
                    }
                )
            }

            composable(Screen.Wishlist.route) {
                WishlistScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { gameId ->
                        navController.navigate(Screen.GameDetail.createRoute(gameId))
                    }
                )
            }

            composable(Screen.Collections.route) {
                CollectionsScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { gameId ->
                        navController.navigate(Screen.GameDetail.createRoute(gameId))
                    }
                )
            }

            composable(Screen.Alerts.route) {
                AlertsScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.GameDetail.route,
                arguments = listOf(navArgument("gameId") { type = NavType.StringType })
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: "elden-ring"
                GameDetailScreen(
                    gameId = gameId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
