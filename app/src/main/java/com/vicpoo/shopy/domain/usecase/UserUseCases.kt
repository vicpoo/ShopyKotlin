package com.vicpoo.shopy.domain.usecase

import com.vicpoo.shopy.domain.model.*
import com.vicpoo.shopy.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class RegisterUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(request: RegisterRequest): AuthResponse = repository.register(request)
}

class LoginUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(request: LoginRequest): AuthResponse = repository.login(request)
}

class LoginWithGoogleUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(idToken: String): AuthResponse = repository.loginWithGoogle(idToken)
}

class GetAllUsersUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): List<User> = repository.getAllUsers()
}

class GetUserByIdUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String): User = repository.getUserById(id)
}

class CreateUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User): User = repository.createUser(user)
}

class UpdateUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User): User = repository.updateUser(user)
}

class DeleteUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String): Boolean = repository.deleteUser(id)
}

class GetCurrentUserUseCase(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<User?> = repository.getCurrentUser()
}

class LogoutUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke() = repository.logout()
}

class ChangeUserRoleUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: String, newRole: String): User {
        val user = repository.getUserById(userId)
        val updatedUser = user.copy(role = newRole)
        return repository.updateUser(updatedUser)
    }
}