//UpdateCartQuantityUseCase
package com.vicpoo.shopy.features.domain.usecase

import com.vicpoo.shopy.features.domain.repository.CartRepository

class UpdateCartQuantityUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(productId: String, newQuantity: Int) {
        repository.updateQuantity(productId, newQuantity)
    }
}