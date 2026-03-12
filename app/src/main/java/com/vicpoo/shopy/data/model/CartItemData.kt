package com.vicpoo.shopy.data.model

data class CartItemData(
    val productId: String = "",
    val quantity: Int = 1,
    val selectedSize: String? = null
)