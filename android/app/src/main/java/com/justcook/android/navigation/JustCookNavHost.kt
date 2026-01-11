package com.justcook.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.justcook.feature.auth.navigation.authNavGraph
import com.justcook.feature.bookmarks.BookmarksScreen
import com.justcook.feature.home.HomeScreen
import com.justcook.feature.profile.ChefProfileScreen
import com.justcook.feature.profile.FollowingScreen
import com.justcook.feature.profile.SettingsScreen
import com.justcook.feature.profile.UserProfileScreen
import com.justcook.feature.recipes.CommentsScreen
import com.justcook.feature.recipes.RecipeCreateScreen
import com.justcook.feature.recipes.RecipeDetailScreen
import com.justcook.feature.recipes.RecipeEditScreen
import com.justcook.feature.search.SearchScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Create : Screen("recipe/create")
    data object Bookmarks : Screen("bookmarks")
    data object Profile : Screen("profile")

    data object RecipeDetail : Screen("recipe/{slug}") {
        fun createRoute(slug: String) = "recipe/$slug"
    }

    data object RecipeEdit : Screen("recipe/{slug}/edit") {
        fun createRoute(slug: String) = "recipe/$slug/edit"
    }

    data object RecipeComments : Screen("recipe/{slug}/comments") {
        fun createRoute(slug: String) = "recipe/$slug/comments"
    }

    data object ChefProfile : Screen("chef/{username}") {
        fun createRoute(username: String) = "chef/$username"
    }

    data object Following : Screen("following")
    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, Icons.Outlined.Home, "Home"),
    BottomNavItem(Screen.Search.route, Icons.Outlined.Search, "Search"),
    BottomNavItem(Screen.Create.route, Icons.Outlined.Add, "Create"),
    BottomNavItem(Screen.Bookmarks.route, Icons.Outlined.Bookmark, "Saved"),
    BottomNavItem(Screen.Profile.route, Icons.Outlined.Person, "Profile")
)

@Composable
fun JustCookNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Routes that show bottom nav
    val showBottomBar = currentDestination?.hierarchy?.any { dest ->
        bottomNavItems.any { it.route == dest.route }
    } == true

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Main tabs
            composable(Screen.Home.route) {
                HomeScreen(
                    onRecipeClick = { slug ->
                        navController.navigate(Screen.RecipeDetail.createRoute(slug))
                    },
                    onChefClick = { username ->
                        navController.navigate(Screen.ChefProfile.createRoute(username))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onRecipeClick = { slug ->
                        navController.navigate(Screen.RecipeDetail.createRoute(slug))
                    }
                )
            }

            composable(Screen.Create.route) {
                RecipeCreateScreen(
                    onRecipeCreated = { slug ->
                        navController.navigate(Screen.RecipeDetail.createRoute(slug)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Bookmarks.route) {
                BookmarksScreen(
                    onRecipeClick = { slug ->
                        navController.navigate(Screen.RecipeDetail.createRoute(slug))
                    }
                )
            }

            composable(Screen.Profile.route) {
                UserProfileScreen(
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onRecipeClick = { slug ->
                        navController.navigate(Screen.RecipeDetail.createRoute(slug))
                    },
                    onLoginClick = {
                        navController.navigate("auth/login")
                    },
                    onFollowingClick = {
                        navController.navigate(Screen.Following.route)
                    }
                )
            }

            // Recipe detail
            composable(
                route = Screen.RecipeDetail.route,
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug") ?: ""
                RecipeDetailScreen(
                    slug = slug,
                    onBack = { navController.popBackStack() },
                    onEditClick = { navController.navigate(Screen.RecipeEdit.createRoute(slug)) },
                    onCommentsClick = { navController.navigate(Screen.RecipeComments.createRoute(slug)) },
                    onChefClick = { username ->
                        navController.navigate(Screen.ChefProfile.createRoute(username))
                    },
                    onLoginRequired = { navController.navigate("auth/login") }
                )
            }

            // Recipe edit
            composable(
                route = Screen.RecipeEdit.route,
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug") ?: ""
                RecipeEditScreen(
                    slug = slug,
                    onRecipeUpdated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            // Comments
            composable(
                route = Screen.RecipeComments.route,
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) {
                CommentsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Chef profile
            composable(
                route = Screen.ChefProfile.route,
                arguments = listOf(navArgument("username") { type = NavType.StringType })
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""
                ChefProfileScreen(
                    username = username,
                    onBack = { navController.popBackStack() },
                    onRecipeClick = { slug ->
                        navController.navigate(Screen.RecipeDetail.createRoute(slug))
                    }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onSignedOut = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Following
            composable(Screen.Following.route) {
                FollowingScreen(
                    onBack = { navController.popBackStack() },
                    onUserClick = { username ->
                        navController.navigate(Screen.ChefProfile.createRoute(username))
                    }
                )
            }

            // Auth navigation graph
            authNavGraph(navController)
        }
    }
}
