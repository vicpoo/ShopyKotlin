// domain/usecase/ObserveProductByIdUseCase.kt
package com.vicpoo.shopy.domain.usecase

import com.vicpoo.shopy.domain.model.Cloth
import com.vicpoo.shopy.domain.repository.ClothRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProductByIdUseCase @Inject constructor(
    private val clothRepository: ClothRepository
) {
    operator fun invoke(productId: String): Flow<Cloth?> =
        clothRepository.observeProductById(productId)
}