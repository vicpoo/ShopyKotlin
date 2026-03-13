//UserData.kt
package com.vicpoo.shopy.data.model

data class UserData(
    val email: String = "",
    val name: String? = null,
    val role: String = "user",
    val cart: Map<String, Any> = emptyMap()
)