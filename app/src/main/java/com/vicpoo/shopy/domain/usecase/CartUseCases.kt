//CartUseCases.kt
package com.vicpoo.shopy.domain.usecase

import com.vicpoo.shopy.domain.model.CartItem
import com.vicpoo.shopy.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow

class GetCartItemsUseCase(
    private val repository: CartRepository
) {
    operator fun invoke(): Flow<List<CartItem>> = repository.getCartItems()
}

class AddToCartUseCase(
    private val repository: CartRepository
) {
    suspend operator fun invoke(productId: String, quantity: Int = 1, selectedSize: String? = null) {
        repository.addToCart(productId, quantity, selectedSize)
    }
}

class RemoveFromCartUseCase(
    private val repository: CartRepository
) {
    suspend operator fun invoke(productId: String) {
        repository.removeFromCart(productId)
    }
}

class UpdateCartQuantityUseCase(
    private val repository: CartRepository
) {
    suspend operator fun invoke(productId: String, newQuantity: Int) {
        repository.updateQuantity(productId, newQuantity)
    }
}

class ClearCartUseCase(
    private val repository: CartRepository
) {
    suspend operator fun invoke() {
        repository.clearCart()
    }
}