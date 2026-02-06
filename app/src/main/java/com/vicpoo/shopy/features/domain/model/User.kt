//User.kt
package com.vicpoo.shopy.features.domain.model

data class User(
    val id: Int = 0,
    val email: String,
    val password: String = "",
    val name: String? = null,
    val lastname: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String? = null,
    val lastname: String? = null
)

data class AuthResponse(
    val id_usuario: Int,
    val email: String,
    val name: String?,
    val lastname: String?,
    val token: String? = null
)