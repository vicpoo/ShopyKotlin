//AppNavigation.kt
package com.vicpoo.shopy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vicpoo.shopy.core.di.Di
import com.vicpoo.shopy.features.presentation.screens.LoginScreen
import com.vicpoo.shopy.features.presentation.screens.MainScreen
import com.vicpoo.shopy.features.presentation.screens.RegisterScreen
import com.vicpoo.shopy.features.presentation.viewmodels.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel = remember {
        AuthViewModel(
            registerUseCase = Di.registerUseCase,
            loginUseCase = Di.loginUseCase,
            loginWithGoogleUseCase = Di.loginWithGoogleUseCase,
            getCurrentUserUseCase = Di.getCurrentUserUseCase,
            logoutUseCase = Di.logoutUseCase
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                navController = navController,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                navController = navController,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                authViewModel = authViewModel,
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}