//User.kt
package com.vicpoo.shopy.features.domain.model

data class User(
    val uid: String,
    val email: String,
    val name: String? = null,
    val role: String = "user",
    val cart: Map<String, Any> = emptyMap()
) {
    val isSeller: Boolean
        get() = role == "seller"

    val isAdmin: Boolean
        get() = role == "admin"
}