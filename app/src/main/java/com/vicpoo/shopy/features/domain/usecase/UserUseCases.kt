//UserUseCase.kt
package com.vicpoo.shopy.features.domain.usecase

import com.vicpoo.shopy.features.domain.model.*
import com.vicpoo.shopy.features.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class RegisterUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(request: RegisterRequest): AuthResponse = repository.register(request)
}

class LoginUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(request: LoginRequest): AuthResponse = repository.login(request)
}

class LoginWithGoogleUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(idToken: String): AuthResponse = repository.loginWithGoogle(idToken)
}

class GetAllUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): List<User> = repository.getAllUsers()
}

class GetUserByIdUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: String): User = repository.getUserById(id)
}

class CreateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user: User): User = repository.createUser(user)
}

class UpdateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user: User): User = repository.updateUser(user)
}

class DeleteUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: String): Boolean = repository.deleteUser(id)
}

class GetCurrentUserUseCase(private val repository: UserRepository) {
    operator fun invoke(): Flow<User?> = repository.getCurrentUser()
}

class LogoutUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() = repository.logout()
}