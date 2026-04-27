//GenerateCaptchaUSeCase.kt
package com.vicpoo.shopy.domain.usecase


import com.vicpoo.shopy.domain.model.CaptchaImage
import com.vicpoo.shopy.domain.repository.CaptchaRepository

class GenerateCaptchaUseCase(
    private val repository: CaptchaRepository
) {
    fun execute(): List<CaptchaImage> {
        return repository
            .getImages()
            .shuffled()
            .take(4)
    }
}