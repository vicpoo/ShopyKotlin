//MainViewModel.kt
package com.vicpoo.shopy.presentation.viewmodels

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicpoo.shopy.R
import com.vicpoo.shopy.domain.model.Cloth
import com.vicpoo.shopy.domain.model.User
import com.vicpoo.shopy.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getAllClothesUseCase: GetAllClothesUseCase,
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
    private val changeUserRoleUseCase: ChangeUserRoleUseCase,
    private val observeClothesBySellerUseCase: ObserveClothesBySellerUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    // Estados de UI
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _products = MutableStateFlow<List<Cloth>>(emptyList())
    val products: StateFlow<List<Cloth>> = _products.asStateFlow()

    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()

    private val _unreadNotifications = MutableStateFlow(0)
    val unreadNotifications: StateFlow<Int> = _unreadNotifications.asStateFlow()

    private val _isSeller = MutableStateFlow(false)
    val isSeller: StateFlow<Boolean> = _isSeller.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showBecomeSellerDialog = MutableStateFlow(false)
    val showBecomeSellerDialog: StateFlow<Boolean> = _showBecomeSellerDialog.asStateFlow()

    // SoundPool para notificaciones
    private var soundPool: SoundPool? = null
    private var notificationSoundId: Int = 0
    private var isSoundLoaded = false

    // Job para el polling
    private var pollingJob: kotlinx.coroutines.Job? = null

    init {
        setupConnectivityListener()
        initSound()
        observeUser()
    }

    private fun initSound() {
        try {
            Log.d(TAG, "Inicializando SoundPool")
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    isSoundLoaded = true
                    Log.d(TAG, "Sonido cargado correctamente")
                } else {
                    Log.e(TAG, "Error al cargar sonido, status: $status")
                    // Forzar carga para no bloquear
                    isSoundLoaded = true
                }
            }

            // Intentar cargar el sonido
            notificationSoundId = soundPool?.load(context, R.raw.notification_sound, 1) ?: 0
            Log.d(TAG, "Sound ID: $notificationSoundId")

            // Si el ID es 0, algo salió mal
            if (notificationSoundId == 0) {
                isSoundLoaded = true // Forzar para no bloquear
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initSound", e)
            isSoundLoaded = true // Fallback
        }
    }

    private fun playNotificationSound() {
        try {
            if (isSoundLoaded && notificationSoundId != 0) {
                val playResult = soundPool?.play(notificationSoundId, 1f, 1f, 0, 0, 1f)
                Log.d(TAG, "Reproduciendo sonido, resultado: $playResult")
            } else {
                Log.e(TAG, "Sonido no cargado o ID inválido. isSoundLoaded: $isSoundLoaded, soundId: $notificationSoundId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al reproducir sonido", e)
        }
    }

    private fun releaseSound() {
        try {
            pollingJob?.cancel()
            soundPool?.release()
            soundPool = null
            isSoundLoaded = false
            Log.d(TAG, "SoundPool liberado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al liberar SoundPool", e)
        }
    }

    private fun setupConnectivityListener() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                // Cuando volvemos online, refrescamos datos
                refreshProducts()
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback)

            // Verificar estado inicial
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            _isOnline.value = capabilities != null &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _currentUser.value = user
                _isSeller.value = user?.isSeller == true

                if (user != null) {
                    // Iniciar todos los listeners cuando hay usuario
                    loadProductsFromNetwork()
                    startPolling()
                    observeCartCount()
                    observeNotifications()
                } else {
                    // Limpiar datos si no hay usuario
                    pollingJob?.cancel()
                    _products.value = emptyList()
                    _cartItemCount.value = 0
                    _unreadNotifications.value = 0
                }
            }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var lastSize = _products.value.size
            while (true) {
                delay(3000) // Revisar cada 3 segundos
                if (_isOnline.value) {
                    try {
                        Log.d(TAG, "Polling: verificando productos...")
                        val freshProducts = getAllClothesUseCase()

                        if (freshProducts.size > lastSize) {
                            Log.d(TAG, "¡Nuevos productos detectados! Antes: $lastSize, Ahora: ${freshProducts.size}")
                            _products.value = freshProducts
                            playNotificationSound()
                            lastSize = freshProducts.size
                        } else if (freshProducts.size < lastSize) {
                            // Se eliminaron productos
                            _products.value = freshProducts
                            lastSize = freshProducts.size
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error en polling", e)
                    }
                }
            }
        }
    }

    private suspend fun loadProductsFromNetwork() {
        try {
            _isLoading.value = true
            val products = getAllClothesUseCase()
            _products.value = products
            Log.d(TAG, "Productos cargados: ${products.size}")
        } catch (e: Exception) {
            _error.value = "Error al cargar productos: ${e.message}"
            Log.e(TAG, "Error cargando productos", e)
        } finally {
            _isLoading.value = false
        }
    }

    private fun observeCartCount() {
        viewModelScope.launch {
            getCartItemsUseCase().collect { cartItems ->
                _cartItemCount.value = cartItems.sumOf { it.quantity }
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            var lastCount = 0
            while (true) {
                try {
                    val count = getUnreadNotificationCountUseCase()
                    if (count > lastCount) {
                        Log.d(TAG, "¡Nueva notificación detectada! ($count)")
                        playNotificationSound()
                    }
                    _unreadNotifications.value = count
                    lastCount = count
                } catch (e: Exception) {
                    Log.e(TAG, "Error observando notificaciones", e)
                }
                delay(3000)
            }
        }
    }

    fun refreshProducts() {
        if (_isRefreshing.value || !_isOnline.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val freshProducts = getAllClothesUseCase()
                _products.value = freshProducts
                Log.d(TAG, "Productos refrescados: ${freshProducts.size}")
            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
                Log.e(TAG, "Error refrescando", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun addToCart(productId: String, productName: String) {
        viewModelScope.launch {
            try {
                addToCartUseCase(productId)
                Log.d(TAG, "Producto agregado al carrito: $productName")
            } catch (e: Exception) {
                _error.value = "Error al agregar al carrito"
                Log.e(TAG, "Error adding to cart", e)
            }
        }
    }

    fun showBecomeSellerDialog() {
        _showBecomeSellerDialog.value = true
    }

    fun hideBecomeSellerDialog() {
        _showBecomeSellerDialog.value = false
    }

    fun becomeSeller() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            _isLoading.value = true
            try {
                val updatedUser = changeUserRoleUseCase(user.uid, "seller")
                _currentUser.value = updatedUser
                _isSeller.value = true
                _showBecomeSellerDialog.value = false

                // Refrescar productos para ver cambios
                refreshProducts()
            } catch (e: Exception) {
                _error.value = "Error al convertirse en vendedor: ${e.message}"
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

    override fun onCleared() {
        super.onCleared()
        releaseSound()
    }
}