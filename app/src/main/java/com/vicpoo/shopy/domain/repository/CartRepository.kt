package com.vicpoo.shopy.domain.repository

import com.vicpoo.shopy.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    suspend fun addToCart(productId: String, quantity: Int = 1, selectedSize: String? = null)
    suspend fun removeFromCart(productId: String)
    suspend fun updateQuantity(productId: String, newQuantity: Int)
    suspend fun clearCart()
}