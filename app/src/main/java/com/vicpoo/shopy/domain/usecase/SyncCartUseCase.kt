// domain/usecase/SyncCartUseCase.kt
package com.vicpoo.shopy.domain.usecase

import com.vicpoo.shopy.data.repository.SyncCartRepository
import javax.inject.Inject

class SyncCartUseCase @Inject constructor(
    private val syncCartRepository: SyncCartRepository
) {
    suspend operator fun invoke() {
        syncCartRepository.trySync()
    }

    suspend fun syncFromFirebase() {
        syncCartRepository.syncFromFirebase()
    }
}