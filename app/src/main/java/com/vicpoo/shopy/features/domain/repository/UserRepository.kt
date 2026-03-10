//UserRepository.kt
package com.vicpoo.shopy.features.domain.repository

import com.vicpoo.shopy.features.domain.model.*
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun loginWithGoogle(idToken: String): AuthResponse
    suspend fun getAllUsers(): List<User>
    suspend fun getUserById(id: String): User
    suspend fun createUser(user: User): User
    suspend fun updateUser(user: User): User
    suspend fun deleteUser(id: String): Boolean
    fun getCurrentUser(): Flow<User?>
    suspend fun logout()
}