//ClothViewModel
package com.vicpoo.shopy.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicpoo.shopy.domain.model.Cloth
import com.vicpoo.shopy.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ClothViewModel @Inject constructor(
    private val getAllClothesUseCase: GetAllClothesUseCase,
    private val createClothUseCase: CreateClothUseCase,
    private val updateClothUseCase: UpdateClothUseCase,
    private val deleteClothUseCase: DeleteClothUseCase,
    private val searchClothByNameUseCase: SearchClothByNameUseCase,
    private val searchClothBySizeUseCase: SearchClothBySizeUseCase,
    private val searchClothByPriceRangeUseCase: SearchClothByPriceRangeUseCase
) : ViewModel() {

    private val _clothes = MutableStateFlow<List<Cloth>>(emptyList())
    val clothes: StateFlow<List<Cloth>> = _clothes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Cloth>>(emptyList())
    val searchResults: StateFlow<List<Cloth>> = _searchResults.asStateFlow()

    private var initialLoadDone = false

    init {
        loadClothes()
    }

    fun loadClothes(forceRefresh: Boolean = false) {
        if (_isLoading.value || (_clothes.value.isNotEmpty() && !forceRefresh && initialLoadDone)) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val clothesList = getAllClothesUseCase()
                _clothes.value = clothesList
                initialLoadDone = true
            } catch (e: Exception) {
                _error.value = "Error al cargar prendas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshClothes() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null

            try {
                val clothesList = getAllClothesUseCase()
                _clothes.value = clothesList
            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun createCloth(cloth: Cloth, imageFile: File?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val newCloth = createClothUseCase(cloth, imageFile)
                _clothes.value = _clothes.value + newCloth
                delay(500)
                refreshClothes()
            } catch (e: Exception) {
                _error.value = "Error al crear prenda: ${e.message}"
                _isLoading.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCloth(id: String, cloth: Cloth, imageFile: File?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val updatedCloth = updateClothUseCase(id, cloth, imageFile)
                _clothes.value = _clothes.value.map {
                    if (it.id == updatedCloth.id) updatedCloth else it
                }
                delay(500)
                refreshClothes()
            } catch (e: Exception) {
                _error.value = "Error al actualizar prenda: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCloth(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = deleteClothUseCase(id)
                if (success) {
                    _clothes.value = _clothes.value.filter { it.id != id }
                    delay(500)
                    refreshClothes()
                } else {
                    _error.value = "No se pudo eliminar la prenda"
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar prenda: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchByName(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val results = searchClothByNameUseCase(name)
                _searchResults.value = results
            } catch (e: Exception) {
                _error.value = "Error al buscar prendas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchBySize(size: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val results = searchClothBySizeUseCase(size)
                _searchResults.value = results
            } catch (e: Exception) {
                _error.value = "Error al buscar por talla: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchByPriceRange(minPrice: Double, maxPrice: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val results = searchClothByPriceRangeUseCase(minPrice, maxPrice)
                _searchResults.value = results
            } catch (e: Exception) {
                _error.value = "Error al buscar por precio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    fun clearError() {
        _error.value = null
    }
}