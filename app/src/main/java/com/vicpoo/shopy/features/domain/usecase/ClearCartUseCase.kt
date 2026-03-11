//ClearCartUseCase.kt
package com.vicpoo.shopy.features.domain.usecase

import com.vicpoo.shopy.features.domain.repository.CartRepository

class ClearCartUseCase(private val repository: CartRepository) {
    suspend operator fun invoke() {
        repository.clearCart()
    }
}