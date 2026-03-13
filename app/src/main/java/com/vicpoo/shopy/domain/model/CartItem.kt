//CartItem.kt
package com.vicpoo.shopy.domain.model

data class CartItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val image: String? = null,
    val quantity: Int = 1,
    val selectedSize: String? = null
)