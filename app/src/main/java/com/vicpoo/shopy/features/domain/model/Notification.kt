//Notification.kt
package com.vicpoo.shopy.features.domain.model

data class Notification(
    val id: String = "",
    val title: String,
    val message: String,
    val productId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)