//ClothData.kt
package com.vicpoo.shopy.data.model

data class ClothData(
    val name: String = "",
    val description: String? = null,
    val size: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
    val image: String? = null,
    val sellerId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)