//ReviewData.kt
package com.vicpoo.shopy.data.model

data class ReviewData(
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
)