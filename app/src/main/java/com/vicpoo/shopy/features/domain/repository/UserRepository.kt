//UserRepository.kt
package com.vicpoo.shopy.features.domain.repository

import com.vicpoo.shopy.features.domain.model.AuthResponse
import com.vicpoo.shopy.features.domain.model.LoginRequest
import com.vicpoo.shopy.features.domain.model.RegisterRequest
import com.vicpoo.shopy.features.domain.model.User

interface UserRepository {
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun getAllUsers(): List<User>
    suspend fun getUserById(id: Int): User
    suspend fun createUser(user: User): User
    suspend fun updateUser(user: User): User
    suspend fun deleteUser(id: Int): Boolean
}