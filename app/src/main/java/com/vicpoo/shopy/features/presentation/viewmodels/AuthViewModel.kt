//AuthViewModel.kt
package com.vicpoo.shopy.features.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicpoo.shopy.features.domain.model.AuthResponse
import com.vicpoo.shopy.features.domain.model.LoginRequest
import com.vicpoo.shopy.features.domain.model.RegisterRequest
import com.vicpoo.shopy.features.domain.usecase.LoginUseCase
import com.vicpoo.shopy.features.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<AuthResponse?>(null)
    val currentUser: StateFlow<AuthResponse?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val user = registerUseCase(request)
                _currentUser.value = user
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val user = loginUseCase(request)
                _currentUser.value = user
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun clearError() {
        _error.value = null
    }
}