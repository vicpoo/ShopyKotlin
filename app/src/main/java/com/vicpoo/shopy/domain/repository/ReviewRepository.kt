//ReviewRepository.kt
package com.vicpoo.shopy.domain.repository

import com.vicpoo.shopy.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getReviewsForProduct(productId: String): Flow<List<Review>>
    suspend fun addReview(productId: String, review: Review): Review
    suspend fun updateReview(productId: String, reviewId: String, review: Review): Review
    suspend fun deleteReview(productId: String, reviewId: String): Boolean
    suspend fun getUserReviewForProduct(productId: String, userId: String): Review?
    suspend fun getAverageRating(productId: String): Double
}