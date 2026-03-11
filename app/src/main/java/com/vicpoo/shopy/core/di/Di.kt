//Di.kt
package com.vicpoo.shopy.core.di

import com.vicpoo.shopy.features.data.repository.*
import com.vicpoo.shopy.features.domain.repository.*
import com.vicpoo.shopy.features.domain.usecase.*

object Di {
    // Repositorios
    private val userRepository: UserRepository by lazy {
        FirebaseUserRepository()
    }

    private val clothRepository: ClothRepository by lazy {
        FirebaseClothRepository()
    }

    private val cartRepository: CartRepository by lazy {
        FirebaseCartRepository()
    }

    // User UseCases
    val registerUseCase: RegisterUseCase by lazy {
        RegisterUseCase(userRepository)
    }

    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(userRepository)
    }

    val loginWithGoogleUseCase: LoginWithGoogleUseCase by lazy {
        LoginWithGoogleUseCase(userRepository)
    }

    val getAllUsersUseCase: GetAllUsersUseCase by lazy {
        GetAllUsersUseCase(userRepository)
    }

    val getUserByIdUseCase: GetUserByIdUseCase by lazy {
        GetUserByIdUseCase(userRepository)
    }

    val createUserUseCase: CreateUserUseCase by lazy {
        CreateUserUseCase(userRepository)
    }

    val updateUserUseCase: UpdateUserUseCase by lazy {
        UpdateUserUseCase(userRepository)
    }

    val deleteUserUseCase: DeleteUserUseCase by lazy {
        DeleteUserUseCase(userRepository)
    }

    val getCurrentUserUseCase: GetCurrentUserUseCase by lazy {
        GetCurrentUserUseCase(userRepository)
    }

    val logoutUseCase: LogoutUseCase by lazy {
        LogoutUseCase(userRepository)
    }

    val changeUserRoleUseCase: ChangeUserRoleUseCase by lazy {
        ChangeUserRoleUseCase(userRepository)
    }

    // Cloth UseCases
    val getAllClothesUseCase: GetAllClothesUseCase by lazy {
        GetAllClothesUseCase(clothRepository)
    }

    val getClothByIdUseCase: GetClothByIdUseCase by lazy {
        GetClothByIdUseCase(clothRepository)
    }

    val createClothUseCase: CreateClothUseCase by lazy {
        CreateClothUseCase(clothRepository)
    }

    val updateClothUseCase: UpdateClothUseCase by lazy {
        UpdateClothUseCase(clothRepository)
    }

    val deleteClothUseCase: DeleteClothUseCase by lazy {
        DeleteClothUseCase(clothRepository)
    }

    val getClothesBySellerUseCase: GetClothesBySellerUseCase by lazy {
        GetClothesBySellerUseCase(clothRepository)
    }

    val observeClothesBySellerUseCase: ObserveClothesBySellerUseCase by lazy {
        ObserveClothesBySellerUseCase(clothRepository)
    }

    val searchClothByNameUseCase: SearchClothByNameUseCase by lazy {
        SearchClothByNameUseCase(clothRepository)
    }

    val searchClothBySizeUseCase: SearchClothBySizeUseCase by lazy {
        SearchClothBySizeUseCase(clothRepository)
    }

    val searchClothByPriceRangeUseCase: SearchClothByPriceRangeUseCase by lazy {
        SearchClothByPriceRangeUseCase(clothRepository)
    }

    // Cart UseCases
    val getCartItemsUseCase: GetCartItemsUseCase by lazy {
        GetCartItemsUseCase(cartRepository)
    }

    val addToCartUseCase: AddToCartUseCase by lazy {
        AddToCartUseCase(cartRepository)
    }

    val removeFromCartUseCase: RemoveFromCartUseCase by lazy {
        RemoveFromCartUseCase(cartRepository)
    }

    val updateCartQuantityUseCase: UpdateCartQuantityUseCase by lazy {
        UpdateCartQuantityUseCase(cartRepository)
    }

    val clearCartUseCase: ClearCartUseCase by lazy {
        ClearCartUseCase(cartRepository)
    }
}