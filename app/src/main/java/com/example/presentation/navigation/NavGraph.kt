package com.example.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ChatApplication
import com.example.presentation.ViewModelFactory
import com.example.presentation.auth.AuthViewModel
import com.example.presentation.auth.LoginScreen
import com.example.presentation.auth.RegisterScreen
import com.example.presentation.auth.ForgotPasswordScreen
import com.example.presentation.auth.SplashScreen
import com.example.presentation.chat.ChatDetailScreen
import com.example.presentation.chat.ChatListScreen
import com.example.presentation.chat.ChatViewModel
import com.example.presentation.chat.ProfileScreen
import com.example.presentation.chat.SettingsScreen
import com.example.presentation.chat.SearchScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    val context = LocalContext.current
    val chatApplication = remember { context.applicationContext as ChatApplication }
    val factory = remember { ViewModelFactory(chatApplication.container) }

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val chatViewModel: ChatViewModel = viewModel(factory = factory)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                viewModel = authViewModel,
                onNavigateNext = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.ChatList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.ChatList.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ChatList.route) {
            ChatListScreen(
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,
                onNavigateToChatDetail = { userId, userName ->
                    navController.navigate(Screen.ChatDetail.createRoute(userId, userName))
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ChatList.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                authViewModel = authViewModel,
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ChatList.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                chatViewModel = chatViewModel,
                onNavigateToChat = { userId, userName ->
                    navController.navigate(Screen.ChatDetail.createRoute(userId, userName)) {
                        popUpTo(Screen.Search.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""

            ChatDetailScreen(
                userId = userId,
                userName = userName,
                viewModel = chatViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
