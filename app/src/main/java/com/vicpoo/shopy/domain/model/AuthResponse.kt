//AuthResponse.kt
package com.vicpoo.shopy.domain.model

data class AuthResponse(
    val uid: String,
    val email: String,
    val name: String? = null,
    val role: String = "user"
)