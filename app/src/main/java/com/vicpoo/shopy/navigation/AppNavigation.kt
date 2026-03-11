// AppNavigation.kt (CORREGIDO)
package com.vicpoo.shopy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    object Cart : Screen("cart") // NUEVA RUTA
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Crear ViewModels que necesitan ser compartidos
    val authViewModel = remember {
        AuthViewModel(
            registerUseCase = Di.registerUseCase,
            loginUseCase = Di.loginUseCase,
            loginWithGoogleUseCase = Di.loginWithGoogleUseCase,
            getCurrentUserUseCase = Di.getCurrentUserUseCase,
            logoutUseCase = Di.logoutUseCase
        )
    }

    // NUEVO: Crear CartViewModel a nivel superior para que sea accesible desde varias pantallas
    val cartViewModel = remember {
        CartViewModel(
            getCartItemsUseCase = Di.getCartItemsUseCase,
            addToCartUseCase = Di.addToCartUseCase,
            removeFromCartUseCase = Di.removeFromCartUseCase,
            updateCartQuantityUseCase = Di.updateCartQuantityUseCase,
            clearCartUseCase = Di.clearCartUseCase
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
                cartViewModel = cartViewModel, // Pasar el VM
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
                onNavigateToCart = { // NUEVO: Acción para ir al carrito
                    navController.navigate(Screen.Cart.route)
                }
            )
        }

        composable(Screen.Seller.route) {
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
        }

        // NUEVA PANTALLA: Carrito
        composable(Screen.Cart.route) {
            CartScreen(
                cartViewModel = cartViewModel,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }
    }
}