//GetCartItemsUseCase.kt
package com.vicpoo.shopy.features.domain.usecase

import com.vicpoo.shopy.features.domain.model.CartItem
import com.vicpoo.shopy.features.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow

class GetCartItemsUseCase(private val repository: CartRepository) {
    operator fun invoke(): Flow<List<CartItem>> = repository.getCartItems()
}