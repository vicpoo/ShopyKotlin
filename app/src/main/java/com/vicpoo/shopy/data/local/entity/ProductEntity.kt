// data/local/entity/ProductEntity.kt
package com.vicpoo.shopy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vicpoo.shopy.domain.model.Cloth

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val size: String?,
    val price: Double?,
    val stock: Int?,
    val image: String?,
    val sellerId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSynced: Long = System.currentTimeMillis()
) {
    fun toDomain(): Cloth = Cloth(
        id = id,
        name = name,
        description = description,
        size = size,
        price = price,
        stock = stock,
        image = image,
        sellerId = sellerId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(cloth: Cloth): ProductEntity = ProductEntity(
            id = cloth.id,
            name = cloth.name,
            description = cloth.description,
            size = cloth.size,
            price = cloth.price,
            stock = cloth.stock,
            image = cloth.image,
            sellerId = cloth.sellerId,
            createdAt = cloth.createdAt,
            updatedAt = cloth.updatedAt
        )
    }
}