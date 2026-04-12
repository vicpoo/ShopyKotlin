// presentation/viewmodels/SellerProductDetailViewModel.kt
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
class SellerProductDetailViewModel @Inject constructor(
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    val showDeleteConfirmDialog: StateFlow<Boolean> = _showDeleteConfirmDialog.asStateFlow()

    private var _reviewToDelete = MutableStateFlow<Review?>(null)
    val reviewToDelete: StateFlow<Review?> = _reviewToDelete.asStateFlow()

    private var currentUserId: String? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                currentUserId = user?.uid
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
                            Log.d("SellerProductDetailVM", "🔄 Producto actualizado: rating=${productData.averageRating}, total=${productData.totalReviews}")
                        }
                    }
                }

                // ✅ Observar reseñas en tiempo real
                launch {
                    reviewRepository.getReviewsForProduct(productId).collect { reviewsList ->
                        _reviews.value = reviewsList
                        Log.d("SellerProductDetailVM", "🔄 Reseñas actualizadas: ${reviewsList.size}")
                    }
                }

            } catch (e: Exception) {
                _error.value = e.message
                Log.e("SellerProductDetailVM", "Error loading product", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun confirmDeleteReview(review: Review) {
        _reviewToDelete.value = review
        _showDeleteConfirmDialog.value = true
    }

    fun hideDeleteConfirmDialog() {
        _showDeleteConfirmDialog.value = false
        _reviewToDelete.value = null
    }

    fun deleteReview() {
        viewModelScope.launch {
            val productId = _productId.value ?: return@launch
            val review = _reviewToDelete.value ?: return@launch

            _isLoading.value = true

            try {
                reviewRepository.deleteReview(productId, review.id)
                Log.d("SellerProductDetailVM", "✅ Review eliminada")
                hideDeleteConfirmDialog()
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("SellerProductDetailVM", "Error deleting review", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}