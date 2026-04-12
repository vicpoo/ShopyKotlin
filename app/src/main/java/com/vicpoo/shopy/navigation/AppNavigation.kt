// AppNavigation.kt
package com.vicpoo.shopy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vicpoo.shopy.presentation.screens.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Seller : Screen("seller")
    object Cart : Screen("cart")
    object Notifications : Screen("notifications")
    object ProductDetail : Screen("product_detail/{productId}")
    object SellerProductDetail : Screen("seller_product_detail/{productId}")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel = hiltViewModel<com.vicpoo.shopy.presentation.viewmodels.AuthViewModel>()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
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
            if (currentUser != null) {
                MainScreen(
                    navController = navController,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    },
                    onNavigateToSeller = {
                        navController.navigate(Screen.Seller.route)
                    },
                    onNavigateToCart = {
                        navController.navigate(Screen.Cart.route)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Screen.Notifications.route)
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Seller.route) {
            if (currentUser != null) {
                SellerScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Seller.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Cart.route) {
            if (currentUser != null) {
                CartScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Notifications.route) {
            if (currentUser != null) {
                NotificationScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Notifications.route) { inclusive = true }
                    }
                }
            }
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            ProductDetailScreen(
                navController = navController,
                productId = productId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SellerProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            SellerProductDetailScreen(
                navController = navController,
                productId = productId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}