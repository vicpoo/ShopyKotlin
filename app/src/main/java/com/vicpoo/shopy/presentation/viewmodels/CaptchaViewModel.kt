    //CaptchaViewModel.kt
    package com.vicpoo.shopy.presentation.viewmodels

    import androidx.compose.runtime.*
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.vicpoo.shopy.domain.model.CaptchaImage
    import com.vicpoo.shopy.domain.usecase.GenerateCaptchaUseCase
    import com.vicpoo.shopy.domain.usecase.ValidateCaptchaUseCase
    import dagger.hilt.android.lifecycle.HiltViewModel
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.launch
    import javax.inject.Inject

    @HiltViewModel
    class CaptchaViewModel @Inject constructor(
        private val generateCaptcha: GenerateCaptchaUseCase,
        private val validateCaptcha: ValidateCaptchaUseCase
    ) : ViewModel() {

        private val _images = MutableStateFlow<List<CaptchaImage>>(emptyList())
        val images: StateFlow<List<CaptchaImage>> = _images

        private val _selected = MutableStateFlow<Set<Int>>(emptySet())
        val selected: StateFlow<Set<Int>> = _selected

        private val _isValidated = MutableStateFlow<Boolean?>(null)
        val isValidated: StateFlow<Boolean?> = _isValidated

        fun loadCaptcha() {
            _images.value = generateCaptcha.execute()
            _selected.value = emptySet()
            _isValidated.value = null
        }

        fun toggleSelection(id: Int) {
            val currentSelected = _selected.value.toMutableSet()
            if (currentSelected.contains(id)) {
                currentSelected.remove(id)
            } else {
                currentSelected.add(id)
            }
            _selected.value = currentSelected
        }

        fun validate(): Boolean {
            val correctIds = _images.value
                .filter { it.isCorrect }
                .map { it.id }
                .toSet()

            val isValid = validateCaptcha.execute(_selected.value, correctIds)
            _isValidated.value = isValid
            return isValid
        }

        fun reset() {
            _isValidated.value = null
        }
    }