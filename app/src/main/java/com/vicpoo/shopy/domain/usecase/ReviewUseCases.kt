//ReviewUseCases.kt
package com.vicpoo.shopy.domain.usecase

import com.vicpoo.shopy.domain.model.Review
import com.vicpoo.shopy.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveReviewsForProductUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    operator fun invoke(productId: String): Flow<List<Review>> =
        repository.getReviewsForProduct(productId)
}

class AddReviewUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(productId: String, review: Review): Review =
        repository.addReview(productId, review)
}

class UpdateReviewUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(productId: String, reviewId: String, review: Review): Review =
        repository.updateReview(productId, reviewId, review)
}

class DeleteReviewUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(productId: String, reviewId: String): Boolean =
        repository.deleteReview(productId, reviewId)
}

class GetUserReviewForProductUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(productId: String, userId: String): Review? =
        repository.getUserReviewForProduct(productId, userId)
}

class GetAverageRatingUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(productId: String): Double =
        repository.getAverageRating(productId)
}