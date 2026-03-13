// data/local/entity/CartItemEntity.kt
package com.vicpoo.shopy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey
    val productId: String,
    val name: String,
    val price: Double,
    val image: String?,
    val quantity: Int,
    val selectedSize: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)