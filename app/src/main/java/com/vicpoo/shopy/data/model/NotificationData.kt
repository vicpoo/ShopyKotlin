package com.vicpoo.shopy.data.model

data class NotificationData(
    val title: String = "",
    val message: String = "",
    val productId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)