// AddToCartUseCase.kt
package com.vicpoo.shopy.features.domain.usecase

import com.vicpoo.shopy.features.domain.repository.CartRepository

class AddToCartUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(productId: String, quantity: Int = 1, selectedSize: String? = null) {
        repository.addToCart(productId, quantity, selectedSize)
    }
}