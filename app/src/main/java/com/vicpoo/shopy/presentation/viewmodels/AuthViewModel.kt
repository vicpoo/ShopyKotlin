//AuthViewModel.kt
package com.vicpoo.shopy.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicpoo.shopy.domain.model.*
import com.vicpoo.shopy.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
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

    // Estados para el captcha
    private val _showCaptcha = MutableStateFlow(false)
    val showCaptcha: StateFlow<Boolean> = _showCaptcha.asStateFlow()

    private val _pendingAction = MutableStateFlow<PendingAuthAction?>(null)
    val pendingAction: StateFlow<PendingAuthAction?> = _pendingAction.asStateFlow()

    sealed class PendingAuthAction {
        data class Login(val email: String, val password: String) : PendingAuthAction()
        data class Register(val request: RegisterRequest) : PendingAuthAction()
    }

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
                    _currentUser.value = null
                }
            }
        }
    }

    // Solicitar login con captcha
    fun requestLogin(email: String, password: String) {
        _pendingAction.value = PendingAuthAction.Login(email, password)
        _showCaptcha.value = true
    }

    // Solicitar registro con captcha
    fun requestRegister(request: RegisterRequest) {
        _pendingAction.value = PendingAuthAction.Register(request)
        _showCaptcha.value = true
    }

    // Proceder después de que el captcha sea validado
    fun proceedAfterCaptcha() {
        viewModelScope.launch {
            when (val action = _pendingAction.value) {
                is PendingAuthAction.Login -> {
                    login(LoginRequest(action.email, action.password))
                }
                is PendingAuthAction.Register -> {
                    register(action.request)
                }
                null -> {}
            }
            _pendingAction.value = null
            _showCaptcha.value = false
        }
    }

    // Ocultar captcha sin proceder
    fun hideCaptcha() {
        _showCaptcha.value = false
        _pendingAction.value = null
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