package com.vicpoo.shopy.features.domain.usecase

import com.vicpoo.shopy.features.domain.model.Cloth
import com.vicpoo.shopy.features.domain.model.ClothRequest
import com.vicpoo.shopy.features.domain.repository.ClothRepository
import java.io.File

class GetAllClothesUseCase(private val repository: ClothRepository) {
    suspend operator fun invoke(): List<Cloth> = repository.getAllClothes()
}

class GetClothByIdUseCase(private val repository: ClothRepository) {
    suspend operator fun invoke(id: Int): Cloth = repository.getClothById(id)
}

class CreateClothUseCase(private val repository: ClothRepository) {
    suspend operator fun invoke(request: ClothRequest, imageFile: File? = null): Cloth =
        repository.createCloth(request, imageFile)
}

class UpdateClothUseCase(private val repository: ClothRepository) {
    suspend operator fun invoke(id: Int, request: ClothRequest, imageFile: File? = null): Cloth =
        repository.updateCloth(id, request, imageFile)
}

class DeleteClothUseCase(private val repository: ClothRepository) {
    suspend operator fun invoke(id: Int): Boolean = repository.deleteCloth(id)
}

class SearchClothByNameUseCase(private val repository: ClothRepository) {
    suspend operator fun invoke(name: String): List<Cloth> = repository.searchByName(name)
}

class SearchClothBySizeUseCase(private val repository: ClothRepository) {
    suspend operator fun invoke(size: String): List<Cloth> = repository.searchBySize(size)
}

class SearchClothByPriceRangeUseCase(private val repository: ClothRepository) {
    suspend operator fun invoke(minPrice: Double, maxPrice: Double): List<Cloth> =
        repository.searchByPriceRange(minPrice, maxPrice)
}