package com.vicpoo.shopy.features.domain.model

data class Cloth(
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val size: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
    val imageUrl: String? = null
)

data class ClothRequest(
    val name: String,
    val description: String? = null,
    val size: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
    val imageUrl: String? = null
)