package com.vicpoo.shopy.features.data.dto

import com.google.gson.annotations.SerializedName

data class ClothDto(
    @SerializedName("id_clothes") val id: Int? = null,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("size") val size: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("stock") val stock: Int? = null,
    @SerializedName("image_url") val imageUrl: String? = null
)

data class ClothRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("size") val size: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("stock") val stock: Int? = null,
    @SerializedName("image_url") val imageUrl: String? = null
)