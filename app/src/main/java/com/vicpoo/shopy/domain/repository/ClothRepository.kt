//ClothRepository.kt
package com.vicpoo.shopy.domain.repository

import com.vicpoo.shopy.domain.model.Cloth
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ClothRepository {
    suspend fun getAllClothes(): List<Cloth>
    suspend fun getClothById(id: String): Cloth
    suspend fun createCloth(cloth: Cloth, imageFile: File? = null): Cloth
    suspend fun updateCloth(id: String, cloth: Cloth, imageFile: File? = null): Cloth
    suspend fun deleteCloth(id: String): Boolean
    suspend fun getClothesBySeller(sellerId: String): List<Cloth>
    fun observeClothesBySeller(sellerId: String): Flow<List<Cloth>>
    suspend fun searchByName(name: String): List<Cloth>
    suspend fun searchBySize(size: String): List<Cloth>
    suspend fun searchByPriceRange(minPrice: Double, maxPrice: Double): List<Cloth>
}