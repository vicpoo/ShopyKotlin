//UserUseCase.kt
package com.vicpoo.shopy.features.domain.usecase

import com.vicpoo.shopy.features.domain.model.AuthResponse
import com.vicpoo.shopy.features.domain.model.LoginRequest
import com.vicpoo.shopy.features.domain.model.RegisterRequest
import com.vicpoo.shopy.features.domain.model.User
import com.vicpoo.shopy.features.domain.repository.UserRepository

class RegisterUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(request: RegisterRequest): AuthResponse = repository.register(request)
}

class LoginUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(request: LoginRequest): AuthResponse = repository.login(request)
}

class GetAllUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): List<User> = repository.getAllUsers()
}

class GetUserByIdUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: Int): User = repository.getUserById(id)
}

class CreateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user: User): User = repository.createUser(user)
}

class UpdateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user: User): User = repository.updateUser(user)
}

class DeleteUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: Int): Boolean = repository.deleteUser(id)
}