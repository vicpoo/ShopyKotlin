package com.vicpoo.shopy.data.repository

import android.util.Log
import com.google.firebase.database.*
import com.vicpoo.shopy.domain.model.Review
import com.vicpoo.shopy.domain.repository.ReviewRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseReviewRepository @Inject constructor(
    private val database: FirebaseDatabase
) : ReviewRepository {

    companion object {
        private const val TAG = "ReviewRepository"
    }

    private fun getReviewsRef(productId: String) =
        database.getReference("reviews").child(productId)

    override fun getReviewsForProduct(productId: String): Flow<List<Review>> = callbackFlow {
        val reviewsRef = getReviewsRef(productId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reviews = snapshot.children.mapNotNull { dataSnapshot ->
                    val id = dataSnapshot.key ?: return@mapNotNull null
                    val value = dataSnapshot.value as? Map<String, Any> ?: return@mapNotNull null

                    Review(
                        id = id,
                        userId = value["userId"] as? String ?: return@mapNotNull null,
                        userName = value["userName"] as? String ?: return@mapNotNull null,
                        rating = (value["rating"] as? Long)?.toInt() ?: return@mapNotNull null,
                        comment = value["comment"] as? String ?: "",
                        createdAt = (value["createdAt"] as? Long) ?: 0,
                        updatedAt = value["updatedAt"] as? Long
                    )
                }.sortedByDescending { it.createdAt }

                trySend(reviews).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error getting reviews: ${error.message}")
                trySend(emptyList()).isSuccess
            }
        }

        reviewsRef.addValueEventListener(listener)

        awaitClose {
            reviewsRef.removeEventListener(listener)
        }
    }

    override suspend fun addReview(productId: String, review: Review): Review {
        val reviewsRef = getReviewsRef(productId)
        val newReviewRef = reviewsRef.push()
        val reviewId = newReviewRef.key ?: throw Exception("Error generating review ID")

        val reviewMap = mapOf(
            "userId" to review.userId,
            "userName" to review.userName,
            "rating" to review.rating,
            "comment" to review.comment,
            "createdAt" to System.currentTimeMillis()
        )

        newReviewRef.setValue(reviewMap).await()

        // Actualizar promedio y contador en el producto
        updateProductRating(productId)

        return review.copy(id = reviewId, createdAt = System.currentTimeMillis())
    }

    override suspend fun updateReview(productId: String, reviewId: String, review: Review): Review {
        val reviewRef = getReviewsRef(productId).child(reviewId)

        val updates = mapOf(
            "rating" to review.rating,
            "comment" to review.comment,
            "updatedAt" to System.currentTimeMillis()
        )

        reviewRef.updateChildren(updates).await()

        // Actualizar promedio
        updateProductRating(productId)

        return review.copy(updatedAt = System.currentTimeMillis())
    }

    override suspend fun deleteReview(productId: String, reviewId: String): Boolean {
        return try {
            getReviewsRef(productId).child(reviewId).removeValue().await()
            updateProductRating(productId)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting review", e)
            false
        }
    }

    override suspend fun getUserReviewForProduct(productId: String, userId: String): Review? {
        val snapshot = getReviewsRef(productId)
            .orderByChild("userId")
            .equalTo(userId)
            .get()
            .await()

        return snapshot.children.firstOrNull()?.let { dataSnapshot ->
            val id = dataSnapshot.key ?: return@let null
            val value = dataSnapshot.value as? Map<String, Any> ?: return@let null

            Review(
                id = id,
                userId = value["userId"] as? String ?: return@let null,
                userName = value["userName"] as? String ?: return@let null,
                rating = (value["rating"] as? Long)?.toInt() ?: return@let null,
                comment = value["comment"] as? String ?: "",
                createdAt = (value["createdAt"] as? Long) ?: 0,
                updatedAt = value["updatedAt"] as? Long
            )
        }
    }

    override suspend fun getAverageRating(productId: String): Double {
        val snapshot = getReviewsRef(productId).get().await()
        val reviews = snapshot.children.mapNotNull { dataSnapshot ->
            val value = dataSnapshot.value as? Map<String, Any> ?: return@mapNotNull null
            (value["rating"] as? Long)?.toInt()
        }

        return if (reviews.isNotEmpty()) {
            reviews.average()
        } else {
            0.0
        }
    }

    private suspend fun updateProductRating(productId: String) {
        try {
            val averageRating = getAverageRating(productId)
            val totalReviews = getReviewsRef(productId).get().await().children.count()

            val updates = mapOf(
                "averageRating" to averageRating,
                "totalReviews" to totalReviews
            )

            database.getReference("products").child(productId).updateChildren(updates).await()
            Log.d(TAG, "Product $productId updated: rating=$averageRating, total=$totalReviews")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product rating", e)
        }
    }
}