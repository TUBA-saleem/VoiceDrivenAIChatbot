package com.example.childeducation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.childeducation.ui.screens.*

@Composable
fun AppNavigator(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.Login.route
    ) {
        // 🔐 Authentication screen
        composable(NavigationRoutes.Login.route) {
            AuthenticationScreen(navController = navController)
        }

        // 🏠 Home screen with navigation drawer
        composable(NavigationRoutes.Home.route) {
            ChatbotHomeScreen(
                onMicClick = { navController.navigate(NavigationRoutes.Listening.route) },
                onChatClick = { navController.navigate(NavigationRoutes.Chat.route) },
                onDrawerItemClick = { route -> navController.navigate(route) },
                onLogout = {
                    navController.navigate(NavigationRoutes.Login.route) {
                        popUpTo(0) // Clears backstack
                }                    }

        )
        }

        // 🎤 Voice listening screen
        composable(NavigationRoutes.Listening.route) {
            ChatbotListeningScreen(
                onCancel = { navController.popBackStack() }
            )
        }

        // 💬 Chat screen
        composable(NavigationRoutes.Chat.route) {
            ChatbotScreen(navController = navController)
        }

        // ✅ Drawer menu screens
        composable(NavigationRoutes.Profile.route) { ProfileScreen() }
        composable(NavigationRoutes.History.route) { HistoryScreen() }
        composable(NavigationRoutes.Language.route) { LanguageScreen() }
        composable(NavigationRoutes.Info.route) { InfoScreen() }
        composable(NavigationRoutes.Settings.route) { SettingsScreen() }
    }
}
