//RemoveFromCartUseCase.kt
package com.vicpoo.shopy.features.domain.usecase

import com.vicpoo.shopy.features.domain.repository.CartRepository

class RemoveFromCartUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(productId: String) {
        repository.removeFromCart(productId)
    }
}