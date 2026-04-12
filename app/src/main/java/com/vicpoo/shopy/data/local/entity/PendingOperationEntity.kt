// data/local/entity/PendingOperationEntity.kt
package com.vicpoo.shopy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationType: String, // "ADD", "UPDATE", "REMOVE", "CLEAR"
    val productId: String,
    val quantity: Int? = null,
    val selectedSize: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)