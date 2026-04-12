//Review.kt
package com.vicpoo.shopy.domain.model

data class Review(
    val id: String = "",
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
)

data class ProductWithReviews(
    val product: Cloth,
    val reviews: List<Review>,
    val averageRating: Double,
    val totalReviews: Int
)