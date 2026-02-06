package com.vicpoo.shopy.features.data.mapper

import com.vicpoo.shopy.features.data.dto.*
import com.vicpoo.shopy.features.domain.model.*

fun ClothDto.toDomain(): Cloth {
    return Cloth(
        id = id ?: 0,
        name = name,
        description = description,
        size = size,
        price = price,
        stock = stock,
        imageUrl = imageUrl
    )
}

fun Cloth.toDto(): ClothDto {
    return ClothDto(
        id = if (id > 0) id else null,
        name = name,
        description = description,
        size = size,
        price = price,
        stock = stock,
        imageUrl = imageUrl
    )
}

fun ClothRequest.toDto(): ClothRequestDto {
    return ClothRequestDto(
        name = name,
        description = description,
        size = size,
        price = price,
        stock = stock,
        imageUrl = imageUrl
    )
}