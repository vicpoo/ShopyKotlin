//ClothUseCases.kt
package com.vicpoo.shopy.domain.usecase

import com.vicpoo.shopy.domain.model.Cloth
import com.vicpoo.shopy.domain.repository.ClothRepository
import kotlinx.coroutines.flow.Flow
import java.io.File

class GetAllClothesUseCase(
    private val repository: ClothRepository
) {
    suspend operator fun invoke(): List<Cloth> = repository.getAllClothes()
}

class GetClothByIdUseCase(
    private val repository: ClothRepository
) {
    suspend operator fun invoke(id: String): Cloth = repository.getClothById(id)
}

class CreateClothUseCase(
    private val repository: ClothRepository
) {
    suspend operator fun invoke(cloth: Cloth, imageFile: File? = null): Cloth =
        repository.createCloth(cloth, imageFile)
}

class UpdateClothUseCase(
    private val repository: ClothRepository
) {
    suspend operator fun invoke(id: String, cloth: Cloth, imageFile: File? = null): Cloth =
        repository.updateCloth(id, cloth, imageFile)
}

class DeleteClothUseCase(
    private val repository: ClothRepository
) {
    suspend operator fun invoke(id: String): Boolean = repository.deleteCloth(id)
}

class GetClothesBySellerUseCase(
    private val repository: ClothRepository
) {
    suspend operator fun invoke(sellerId: String): List<Cloth> =
        repository.getClothesBySeller(sellerId)
}

class ObserveClothesBySellerUseCase(
    private val repository: ClothRepository
) {
    operator fun invoke(sellerId: String): Flow<List<Cloth>> =
        repository.observeClothesBySeller(sellerId)
}

class SearchClothByNameUseCase(
    private val repository: ClothRepository
) {
    suspend operator fun invoke(name: String): List<Cloth> = repository.searchByName(name)
}

class SearchClothBySizeUseCase(
    private val repository: ClothRepository
) {
    suspend operator fun invoke(size: String): List<Cloth> = repository.searchBySize(size)
}

class SearchClothByPriceRangeUseCase(
    private val repository: ClothRepository
) {
    suspend operator fun invoke(minPrice: Double, maxPrice: Double): List<Cloth> =
        repository.searchByPriceRange(minPrice, maxPrice)
}