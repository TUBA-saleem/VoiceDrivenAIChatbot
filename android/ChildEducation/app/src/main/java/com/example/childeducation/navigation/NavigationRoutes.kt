package com.example.childeducation.navigation

sealed class NavigationRoutes(val route: String) {
    object Home : NavigationRoutes("home")
    object Listening : NavigationRoutes("listening")
    object Chat : NavigationRoutes("chat")
    object Profile : NavigationRoutes("profile")
    object History : NavigationRoutes("history")
    object Language : NavigationRoutes("language")
    object Info : NavigationRoutes("info")
    object Settings : NavigationRoutes("settings")
    object Login : NavigationRoutes("login")
}
