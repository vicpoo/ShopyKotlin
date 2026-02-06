//UserRepositoryImpl.kt
package com.vicpoo.shopy.features.data.repository

import com.vicpoo.shopy.features.data.dto.AuthResponseDto
import com.vicpoo.shopy.features.data.mapper.*
import com.vicpoo.shopy.features.data.remote.UserApi
import com.vicpoo.shopy.features.domain.model.*
import com.vicpoo.shopy.features.domain.repository.UserRepository
import java.io.IOException

class UserRepositoryImpl(
    private val api: UserApi
) : UserRepository {

    override suspend fun register(request: RegisterRequest): AuthResponse {
        return try {
            api.register(request.toDto()).toDomain()
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error al registrar usuario: ${e.message}")
        }
    }

    override suspend fun login(request: LoginRequest): AuthResponse {
        return try {
            api.login(request.toDto()).toDomain()
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Credenciales inválidas")
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            api.getAllUsers().map { it.toDomain() }
        } catch (e: Exception) {
            throw Exception("Error al obtener usuarios: ${e.message}")
        }
    }

    override suspend fun getUserById(id: Int): User {
        return try {
            api.getUserById(id).toDomain()
        } catch (e: Exception) {
            throw Exception("Error al obtener usuario: ${e.message}")
        }
    }

    override suspend fun createUser(user: User): User {
        return try {
            api.createUser(user.toDto()).toDomain()
        } catch (e: Exception) {
            throw Exception("Error al crear usuario: ${e.message}")
        }
    }

    override suspend fun updateUser(user: User): User {
        return try {
            api.updateUser(user.id, user.toDto()).toDomain()
        } catch (e: Exception) {
            throw Exception("Error al actualizar usuario: ${e.message}")
        }
    }

    override suspend fun deleteUser(id: Int): Boolean {
        return try {
            val response = api.deleteUser(id)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}