package com.vicpoo.shopy.domain.usecase

class ValidateCaptchaUseCase {

    fun execute(
        selected: Set<Int>,
        correct: Set<Int>
    ): Boolean {
        return selected == correct
    }
}