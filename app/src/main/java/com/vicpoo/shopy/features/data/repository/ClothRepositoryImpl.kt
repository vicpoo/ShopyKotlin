package com.vicpoo.shopy.features.data.repository

import com.vicpoo.shopy.features.data.mapper.toDomain
import com.vicpoo.shopy.features.data.remote.ClothApi
import com.vicpoo.shopy.features.domain.model.Cloth
import com.vicpoo.shopy.features.domain.model.ClothRequest
import com.vicpoo.shopy.features.domain.repository.ClothRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ClothRepositoryImpl(
    private val api: ClothApi
) : ClothRepository {

    override suspend fun getAllClothes(): List<Cloth> {
        return try {
            api.getAllClothes().map { it.toDomain() }
        } catch (e: Exception) {
            throw Exception("Error al obtener prendas: ${e.message}")
        }
    }

    override suspend fun getClothById(id: Int): Cloth {
        return try {
            api.getClothById(id).toDomain()
        } catch (e: Exception) {
            throw Exception("Error al obtener prenda: ${e.message}")
        }
    }

    override suspend fun createCloth(request: ClothRequest, imageFile: File?): Cloth {
        return try {
            val namePart = request.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = request.description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val sizePart = request.size?.toRequestBody("text/plain".toMediaTypeOrNull())
            val pricePart = request.price?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val stockPart = request.stock?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = imageFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", it.name, requestFile)
            }

            api.createCloth(
                name = namePart,
                description = descriptionPart,
                size = sizePart,
                price = pricePart,
                stock = stockPart,
                image = imagePart
            ).toDomain()
        } catch (e: Exception) {
            throw Exception("Error al crear prenda: ${e.message}")
        }
    }

    override suspend fun updateCloth(id: Int, request: ClothRequest, imageFile: File?): Cloth {
        return try {
            val namePart = request.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = request.description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val sizePart = request.size?.toRequestBody("text/plain".toMediaTypeOrNull())
            val pricePart = request.price?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val stockPart = request.stock?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val existingImagePart = request.imageUrl?.toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = imageFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", it.name, requestFile)
            }

            api.updateCloth(
                id = id,
                name = namePart,
                description = descriptionPart,
                size = sizePart,
                price = pricePart,
                stock = stockPart,
                image = imagePart,
                existingImageUrl = existingImagePart
            ).toDomain()
        } catch (e: Exception) {
            throw Exception("Error al actualizar prenda: ${e.message}")
        }
    }

    override suspend fun deleteCloth(id: Int): Boolean {
        return try {
            val response = api.deleteCloth(id)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun searchByName(name: String): List<Cloth> {
        return try {
            api.searchByName(name).map { it.toDomain() }
        } catch (e: Exception) {
            throw Exception("Error al buscar prendas: ${e.message}")
        }
    }

    override suspend fun searchBySize(size: String): List<Cloth> {
        return try {
            api.searchBySize(size).map { it.toDomain() }
        } catch (e: Exception) {
            throw Exception("Error al buscar por talla: ${e.message}")
        }
    }

    override suspend fun searchByPriceRange(minPrice: Double, maxPrice: Double): List<Cloth> {
        return try {
            api.searchByPriceRange(minPrice, maxPrice).map { it.toDomain() }
        } catch (e: Exception) {
            throw Exception("Error al buscar por precio: ${e.message}")
        }
    }
}