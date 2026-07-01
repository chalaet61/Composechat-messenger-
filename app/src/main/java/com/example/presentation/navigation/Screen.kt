package com.example.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ChatList : Screen("chat_list")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Search : Screen("search")
    
    object ChatDetail : Screen("chat_detail/{userId}/{userName}") {
        fun createRoute(userId: String, userName: String): String {
            return "chat_detail/$userId/$userName"
        }
    }
}
