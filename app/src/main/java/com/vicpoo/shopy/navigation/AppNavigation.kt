// AppNavigation.kt
package com.vicpoo.shopy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vicpoo.shopy.core.di.Di
import com.vicpoo.shopy.features.presentation.screens.*
import com.vicpoo.shopy.features.presentation.viewmodels.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Seller : Screen("seller")
    object Cart : Screen("cart")
    object Notifications : Screen("notifications")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Crear AuthViewModel a nivel global (no requiere autenticación)
    val authViewModel = remember {
        AuthViewModel(
            registerUseCase = Di.registerUseCase,
            loginUseCase = Di.loginUseCase,
            loginWithGoogleUseCase = Di.loginWithGoogleUseCase,
            getCurrentUserUseCase = Di.getCurrentUserUseCase,
            logoutUseCase = Di.logoutUseCase
        )
    }

    // Observar el estado de autenticación
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    // Crear CartViewModel solo cuando hay usuario autenticado
    val cartViewModel = remember(currentUser != null) {
        if (currentUser != null) {
            CartViewModel(
                getCartItemsUseCase = Di.getCartItemsUseCase,
                addToCartUseCase = Di.addToCartUseCase,
                removeFromCartUseCase = Di.removeFromCartUseCase,
                updateCartQuantityUseCase = Di.updateCartQuantityUseCase,
                clearCartUseCase = Di.clearCartUseCase
            )
        } else {
            null
        }
    }

    // Crear NotificationViewModel solo cuando hay usuario autenticado
    val notificationViewModel = remember(currentUser != null) {
        if (currentUser != null) {
            NotificationViewModel(
                createNotificationUseCase = Di.createNotificationUseCase,
                getNotificationsUseCase = Di.getNotificationsUseCase,
                markAsReadUseCase = Di.markNotificationAsReadUseCase,
                markAllAsReadUseCase = Di.markAllNotificationsAsReadUseCase,
                deleteNotificationUseCase = Di.deleteNotificationUseCase,
                getUnreadCountUseCase = Di.getUnreadNotificationCountUseCase
            )
        } else {
            null
        }
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
            // Solo mostrar MainScreen si hay usuario autenticado
            if (currentUser != null && cartViewModel != null && notificationViewModel != null) {
                MainScreen(
                    authViewModel = authViewModel,
                    cartViewModel = cartViewModel,
                    notificationViewModel = notificationViewModel,
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
                // Redirigir al login si no hay usuario
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Seller.route) {
            if (currentUser != null) {
                val sellerViewModel = remember {
                    SellerViewModel(
                        getCurrentUserUseCase = Di.getCurrentUserUseCase,
                        changeUserRoleUseCase = Di.changeUserRoleUseCase,
                        getClothesBySellerUseCase = Di.getClothesBySellerUseCase,
                        observeClothesBySellerUseCase = Di.observeClothesBySellerUseCase,
                        createClothUseCase = Di.createClothUseCase,
                        updateClothUseCase = Di.updateClothUseCase,
                        deleteClothUseCase = Di.deleteClothUseCase
                    )
                }

                SellerScreen(
                    sellerViewModel = sellerViewModel,
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
            if (currentUser != null && cartViewModel != null) {
                CartScreen(
                    cartViewModel = cartViewModel,
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
            if (currentUser != null && notificationViewModel != null) {
                NotificationScreen(
                    notificationViewModel = notificationViewModel,
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    onProductClick = { productId ->
                        navController.popBackStack(Screen.Main.route, false)
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
    }
}