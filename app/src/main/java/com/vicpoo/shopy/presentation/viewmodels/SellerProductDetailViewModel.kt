//SellerProductDetailViewModel.kt
package com.vicpoo.shopy.presentation.viewmodels

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
                // Cargar producto
                val productData = getClothByIdUseCase(productId)
                _product.value = productData

                // Iniciar la recolección de reseñas (esto corre en paralelo)
                launch {
                    reviewRepository.getReviewsForProduct(productId).collect { reviewsList ->
                        _reviews.value = reviewsList
                    }
                }

            } catch (e: Exception) {
                _error.value = e.message
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
                // La recolección de reseñas actualizará automáticamente _reviews
                hideDeleteConfirmDialog()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}