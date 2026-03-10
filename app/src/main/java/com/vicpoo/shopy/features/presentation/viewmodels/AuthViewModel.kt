//AuthViewModel.kt
package com.vicpoo.shopy.features.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicpoo.shopy.core.firebase.FirebaseConfig
import com.vicpoo.shopy.features.domain.model.*
import com.vicpoo.shopy.features.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class AuthViewModel(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<AuthResponse?>(null)
    val currentUser: StateFlow<AuthResponse?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                user?.let {
                    _currentUser.value = AuthResponse(
                        uid = it.uid,
                        email = it.email,
                        name = it.name,
                        role = it.role
                    )
                } ?: run {
                    // Solo limpiamos si realmente no hay usuario
                    if (FirebaseConfig.auth.currentUser == null) {
                        _currentUser.value = null
                    }
                }
            }
        }
    }

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

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val user = loginWithGoogleUseCase(idToken)
                // Pequeña pausa para asegurar que la base de datos se actualice
                delay(500)
                _currentUser.value = user
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _currentUser.value = null
        }
    }

    fun clearError() {
        _error.value = null
    }
}