//RegisterRequest.kt
package com.vicpoo.shopy.features.domain.model

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String? = null,
    val role: String = "user"
)