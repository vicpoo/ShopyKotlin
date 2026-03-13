// data/local/dao/ProductDao.kt
package com.vicpoo.shopy.data.local.dao

import androidx.room.*
import com.vicpoo.shopy.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductEntity>)

    @Transaction
    suspend fun replaceAllProducts(products: List<ProductEntity>) {
        if (products.isEmpty()) {
            clearAllProducts()
            return
        }
        val incomingIds: List<String> = products.map { it.id }
        deleteProductsNotIn(incomingIds)
        insertAllProducts(products)
    }

    @Query("DELETE FROM products WHERE id NOT IN (:ids)")
    suspend fun deleteProductsNotIn(ids: List<String>)

    @Query("DELETE FROM products")
    suspend fun clearAllProducts()

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    suspend fun getAllProductsOnce(): List<ProductEntity>
}