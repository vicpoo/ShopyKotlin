// presentation/viewmodels/ProductDetailViewModel.kt
package com.vicpoo.shopy.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicpoo.shopy.domain.model.Cloth
import com.vicpoo.shopy.domain.model.Review
import com.vicpoo.shopy.domain.repository.ReviewRepository
import com.vicpoo.shopy.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getClothByIdUseCase: GetClothByIdUseCase,
    private val observeProductByIdUseCase: ObserveProductByIdUseCase,
    private val reviewRepository: ReviewRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _productId = MutableStateFlow<String?>(null)

    private val _product = MutableStateFlow<Cloth?>(null)
    val product: StateFlow<Cloth?> = _product.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _userReview = MutableStateFlow<Review?>(null)
    val userReview: StateFlow<Review?> = _userReview.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showAddReviewDialog = MutableStateFlow(false)
    val showAddReviewDialog: StateFlow<Boolean> = _showAddReviewDialog.asStateFlow()

    private val _editingReview = MutableStateFlow<Review?>(null)
    val editingReview: StateFlow<Review?> = _editingReview.asStateFlow()

    private var currentUserId: String? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                currentUserId = user?.uid
                _productId.value?.let { loadUserReview(it) }
            }
        }
    }

    fun loadProduct(productId: String) {
        _productId.value = productId
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // ✅ Observar producto en tiempo real (actualiza rating automáticamente)
                launch {
                    observeProductByIdUseCase(productId).collect { productData ->
                        if (productData != null) {
                            _product.value = productData
                            Log.d("ProductDetailVM", "🔄 Producto actualizado: rating=${productData.averageRating}, total=${productData.totalReviews}")
                        }
                    }
                }

                // ✅ Observar reseñas en tiempo real
                launch {
                    reviewRepository.getReviewsForProduct(productId).collect { reviewsList ->
                        _reviews.value = reviewsList
                        Log.d("ProductDetailVM", "🔄 Reseñas actualizadas: ${reviewsList.size}")
                    }
                }

                // Cargar reseña del usuario actual
                loadUserReview(productId)

            } catch (e: Exception) {
                _error.value = e.message
                Log.e("ProductDetailVM", "Error loading product", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadUserReview(productId: String) {
        currentUserId?.let { userId ->
            try {
                val review = reviewRepository.getUserReviewForProduct(productId, userId)
                _userReview.value = review
                Log.d("ProductDetailVM", "👤 User review: ${review?.rating}")
            } catch (e: Exception) {
                Log.e("ProductDetailVM", "Error loading user review", e)
            }
        }
    }

    fun showAddReviewDialog() {
        _showAddReviewDialog.value = true
    }

    fun hideAddReviewDialog() {
        _showAddReviewDialog.value = false
        _editingReview.value = null
    }

    fun editReview(review: Review) {
        _editingReview.value = review
        _showAddReviewDialog.value = true
    }

    fun submitReview(rating: Int, comment: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null

            try {
                val productId = _productId.value ?: throw Exception("Producto no encontrado")
                val userId = currentUserId ?: throw Exception("Usuario no autenticado")
                val userName = getCurrentUserUseCase().first()?.name ?: "Usuario"

                val existingReview = _userReview.value

                if (existingReview != null) {
                    // Actualizar reseña existente
                    val updatedReview = existingReview.copy(
                        rating = rating,
                        comment = comment,
                        updatedAt = System.currentTimeMillis()
                    )
                    reviewRepository.updateReview(productId, existingReview.id, updatedReview)
                    Log.d("ProductDetailVM", "✅ Review actualizada: rating=$rating")
                    // El listener actualizará automáticamente _userReview
                } else {
                    // Crear nueva reseña
                    val newReview = Review(
                        userId = userId,
                        userName = userName,
                        rating = rating,
                        comment = comment
                    )
                    reviewRepository.addReview(productId, newReview)
                    Log.d("ProductDetailVM", "✅ Review creada: rating=$rating")
                    // El listener actualizará automáticamente _userReview
                }

                hideAddReviewDialog()

            } catch (e: Exception) {
                _error.value = e.message
                Log.e("ProductDetailVM", "Error submitting review", e)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun deleteUserReview() {
        viewModelScope.launch {
            _isSubmitting.value = true

            try {
                val productId = _productId.value ?: throw Exception("Producto no encontrado")
                val review = _userReview.value ?: throw Exception("No hay reseña para eliminar")

                reviewRepository.deleteReview(productId, review.id)
                Log.d("ProductDetailVM", "✅ Review eliminada")
                // El listener actualizará automáticamente _userReview

            } catch (e: Exception) {
                _error.value = e.message
                Log.e("ProductDetailVM", "Error deleting review", e)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}