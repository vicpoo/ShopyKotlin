// CartViewModel.kt
package com.vicpoo.shopy.features.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicpoo.shopy.features.domain.model.CartItem
import com.vicpoo.shopy.features.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CartViewModel(
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val clearCartUseCase: ClearCartUseCase
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val totalPrice: StateFlow<Double> = _cartItems
        .map { items -> items.sumOf { it.price * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    init {
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            getCartItemsUseCase().collect { items ->
                _cartItems.value = items
            }
        }
    }

    fun addToCart(productId: String, quantity: Int = 1) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                addToCartUseCase(productId, quantity)
            } catch (e: Exception) {
                _error.value = "Error al agregar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                removeFromCartUseCase(productId)
            } catch (e: Exception) {
                _error.value = "Error al eliminar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                updateCartQuantityUseCase(productId, newQuantity)
            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                clearCartUseCase()
            } catch (e: Exception) {
                _error.value = "Error al vaciar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}