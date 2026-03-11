// SellerViewModel.kt
package com.vicpoo.shopy.features.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicpoo.shopy.features.domain.model.Cloth
import com.vicpoo.shopy.features.domain.model.User
import com.vicpoo.shopy.features.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class SellerViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val changeUserRoleUseCase: ChangeUserRoleUseCase,
    private val getClothesBySellerUseCase: GetClothesBySellerUseCase,
    private val observeClothesBySellerUseCase: ObserveClothesBySellerUseCase,
    private val createClothUseCase: CreateClothUseCase,
    private val updateClothUseCase: UpdateClothUseCase,
    private val deleteClothUseCase: DeleteClothUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _sellerClothes = MutableStateFlow<List<Cloth>>(emptyList())
    val sellerClothes: StateFlow<List<Cloth>> = _sellerClothes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showConfirmationDialog = MutableStateFlow(false)
    val showConfirmationDialog: StateFlow<Boolean> = _showConfirmationDialog.asStateFlow()

    private val _isSeller = MutableStateFlow(false)
    val isSeller: StateFlow<Boolean> = _isSeller.asStateFlow()

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _currentUser.value = user
                _isSeller.value = user?.isSeller == true

                // Si es vendedor, observamos sus productos
                if (user?.isSeller == true) {
                    observeSellerClothes(user.uid)
                } else {
                    _sellerClothes.value = emptyList()
                }
            }
        }
    }

    private fun observeSellerClothes(sellerId: String) {
        viewModelScope.launch {
            observeClothesBySellerUseCase(sellerId).collect { clothes ->
                _sellerClothes.value = clothes
            }
        }
    }

    fun showBecomeSellerDialog() {
        _showConfirmationDialog.value = true
    }

    fun hideBecomeSellerDialog() {
        _showConfirmationDialog.value = false
    }

    fun becomeSeller() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            _isLoading.value = true
            _error.value = null

            try {
                val updatedUser = changeUserRoleUseCase(user.uid, "seller")
                _currentUser.value = updatedUser
                _isSeller.value = true
                _showConfirmationDialog.value = false

                // Empezar a observar sus productos ahora que es vendedor
                observeSellerClothes(updatedUser.uid)
            } catch (e: Exception) {
                _error.value = "Error al convertirse en vendedor: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createCloth(cloth: Cloth, imageFile: File?) {
        viewModelScope.launch {
            val sellerId = _currentUser.value?.uid ?: return@launch

            _isLoading.value = true
            _error.value = null

            try {
                val newCloth = cloth.copy(sellerId = sellerId)
                createClothUseCase(newCloth, imageFile)
                // La lista se actualizará automáticamente por el observer
            } catch (e: Exception) {
                _error.value = "Error al crear prenda: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCloth(cloth: Cloth, imageFile: File?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                updateClothUseCase(cloth.id, cloth, imageFile)
                // La lista se actualizará automáticamente por el observer
            } catch (e: Exception) {
                _error.value = "Error al actualizar prenda: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCloth(clothId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                deleteClothUseCase(clothId)
                // La lista se actualizará automáticamente por el observer
            } catch (e: Exception) {
                _error.value = "Error al eliminar prenda: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}