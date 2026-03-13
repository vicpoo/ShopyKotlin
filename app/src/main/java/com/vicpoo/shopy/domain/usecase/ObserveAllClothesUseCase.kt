// domain/usecase/ObserveAllClothesUseCase.kt
package com.vicpoo.shopy.domain.usecase

import com.vicpoo.shopy.domain.model.Cloth
import com.vicpoo.shopy.domain.repository.ClothRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllClothesUseCase @Inject constructor(
    private val clothRepository: ClothRepository
) {
    operator fun invoke(): Flow<List<Cloth>> = clothRepository.observeAllClothes()
}