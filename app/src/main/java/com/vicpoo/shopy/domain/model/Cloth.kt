//Cloth.kt
package com.vicpoo.shopy.domain.model

data class Cloth(
    val id: String = "",
    val name: String,
    val description: String? = null,
    val size: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
    val image: String? = null,
    val sellerId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0
)