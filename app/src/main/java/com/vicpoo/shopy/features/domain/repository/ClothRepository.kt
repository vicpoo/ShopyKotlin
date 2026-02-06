package com.vicpoo.shopy.features.domain.repository

import com.vicpoo.shopy.features.domain.model.Cloth
import com.vicpoo.shopy.features.domain.model.ClothRequest
import java.io.File

interface ClothRepository {
    suspend fun getAllClothes(): List<Cloth>
    suspend fun getClothById(id: Int): Cloth
    suspend fun createCloth(request: ClothRequest, imageFile: File? = null): Cloth
    suspend fun updateCloth(id: Int, request: ClothRequest, imageFile: File? = null): Cloth
    suspend fun deleteCloth(id: Int): Boolean
    suspend fun searchByName(name: String): List<Cloth>
    suspend fun searchBySize(size: String): List<Cloth>
    suspend fun searchByPriceRange(minPrice: Double, maxPrice: Double): List<Cloth>
}