//Di.kt
package com.vicpoo.shopy.core.di

import com.vicpoo.shopy.features.data.repository.FirebaseUserRepository
import com.vicpoo.shopy.features.domain.repository.UserRepository
import com.vicpoo.shopy.features.domain.usecase.*

object Di {
    private val userRepository: UserRepository by lazy {
        FirebaseUserRepository()
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

    // Aquí irán los use cases de productos cuando los implementes
    // val getAllClothesUseCase: GetAllClothesUseCase by lazy { ... }
}