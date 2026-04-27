package com.vicpoo.shopy.presentation.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.vicpoo.shopy.domain.model.CaptchaImage
import com.vicpoo.shopy.domain.usecase.GenerateCaptchaUseCase
import com.vicpoo.shopy.domain.usecase.ValidateCaptchaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CaptchaViewModel @Inject constructor(
    private val generateCaptcha: GenerateCaptchaUseCase,
    private val validateCaptcha: ValidateCaptchaUseCase
) : ViewModel() {

    var images = mutableStateOf<List<CaptchaImage>>(emptyList())
        private set

    var selected = mutableStateListOf<Int>()
        private set

    fun loadCaptcha() {
        images.value = generateCaptcha.execute()
        selected.clear()
    }

    fun toggleSelection(id: Int) {
        if (selected.contains(id)) {
            selected.remove(id)
        } else {
            selected.add(id)
        }
    }

    fun validate(): Boolean {
        val correctIds = images.value
            .filter { it.isCorrect }
            .map { it.id }
            .toSet()

        return validateCaptcha.execute(
            selected.toSet(),
            correctIds
        )
    }
}