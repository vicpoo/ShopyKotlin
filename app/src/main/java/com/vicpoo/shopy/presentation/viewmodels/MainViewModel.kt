// presentation/viewmodels/MainViewModel.kt
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
import com.vicpoo.shopy.core.utils.NotificationHelper
import com.vicpoo.shopy.data.local.dao.ProductDao
import com.vicpoo.shopy.data.local.entity.ProductEntity
import com.vicpoo.shopy.domain.model.Cloth
import com.vicpoo.shopy.domain.model.User
import com.vicpoo.shopy.domain.usecase.*
import com.vicpoo.shopy.domain.work.WorkManagerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
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
    private val observeAllClothesUseCase: ObserveAllClothesUseCase,
    private val observeClothesBySellerUseCase: ObserveClothesBySellerUseCase,
    private val syncCartUseCase: SyncCartUseCase,
    private val workManagerHelper: WorkManagerHelper,
    private val productDao: ProductDao,
    private val notificationHelper: NotificationHelper, // ✅ INYECTADO
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val products: StateFlow<List<Cloth>> = productDao
        .getAllProducts()
        .map { entities: List<ProductEntity> -> entities.map { it.toDomain() } }
        .catch { e ->
            Log.e(TAG, "Error observando Room", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

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

    private var soundPool: SoundPool? = null
    private var notificationSoundId: Int = 0
    private var isSoundLoaded = false

    private var firebaseJob: Job? = null
    private var notificationJob: Job? = null

    init {
        initSound()
        setupConnectivityListener()
        observeUser()
        subscribeToPushTopics()
    }

    private fun initSound() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build()
            soundPool?.setOnLoadCompleteListener { _, _, status ->
                isSoundLoaded = status == 0
            }
            notificationSoundId = soundPool?.load(context, R.raw.notification_sound, 1) ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error initSound", e)
        }
    }

    private fun playNotificationSound() {
        try {
            if (isSoundLoaded && notificationSoundId != 0) {
                soundPool?.play(notificationSoundId, 1f, 1f, 0, 0, 1f)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reproduciendo sonido", e)
        }
    }

    private fun releaseSound() {
        soundPool?.release()
        soundPool = null
        isSoundLoaded = false
    }

    /**
     * Suscribir el dispositivo a topics de notificaciones push
     */
    private fun subscribeToPushTopics() {
        viewModelScope.launch {
            try {
                // ✅ Usar la instancia inyectada
                notificationHelper.subscribeToTopic("all_users")

                // Cuando el usuario esté autenticado, suscribir a su topic personal
                getCurrentUserUseCase().collect { user ->
                    user?.let {
                        val userTopic = "user_${it.uid}"
                        notificationHelper.subscribeToTopic(userTopic)
                        Log.d(TAG, "✅ Suscrito a topic personal: $userTopic")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error suscribiendo a topics", e)
            }
        }
    }

    private fun setupConnectivityListener() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "📶 Online")
                _isOnline.value = true
                viewModelScope.launch {
                    startFirebaseSync()
                    syncCartUseCase()
                    syncCartUseCase.syncFromFirebase()
                    workManagerHelper.syncNow()
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "📴 Offline — usando Room")
                _isOnline.value = false
                stopFirebaseSync()
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            cm.registerNetworkCallback(request, callback)
            val active = cm.getNetworkCapabilities(cm.activeNetwork)
            _isOnline.value = active?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            Log.e(TAG, "Error registrando network callback", e)
        }
    }

    private fun startFirebaseSync() {
        stopFirebaseSync()
        if (!_isOnline.value) return

        Log.d(TAG, "🔥 Iniciando sync Firebase → Room")

        firebaseJob = viewModelScope.launch {
            launch {
                observeAllClothesUseCase()
                    .catch { e ->
                        Log.e(TAG, "Error listener todos los productos", e)
                        emit(emptyList())
                    }
                    .collect { firebaseProducts: List<Cloth> ->
                        Log.d(TAG, "🔄 Firebase → ${firebaseProducts.size} productos → Room")
                        val entities = firebaseProducts.map { ProductEntity.fromDomain(it) }
                        productDao.replaceAllProducts(entities)
                    }
            }

            val uid = _currentUser.value?.uid
            if (_isSeller.value && uid != null) {
                launch {
                    observeClothesBySellerUseCase(uid)
                        .catch { e ->
                            Log.e(TAG, "Error listener vendedor", e)
                            emit(emptyList())
                        }
                        .collect { sellerProducts: List<Cloth> ->
                            Log.d(TAG, "👤 Productos vendedor: ${sellerProducts.size}")
                            if (sellerProducts.isNotEmpty()) {
                                val entities = sellerProducts.map { ProductEntity.fromDomain(it) }
                                productDao.insertAllProducts(entities)
                            }
                        }
                }
            }
        }
    }

    private fun stopFirebaseSync() {
        firebaseJob?.cancel()
        firebaseJob = null
        Log.d(TAG, "🛑 Firebase sync detenido")
    }

    private fun observeUser() {
        viewModelScope.launch {
            getCurrentUserUseCase()
                .catch { e ->
                    Log.e(TAG, "Error observando usuario", e)
                    emit(null)
                }
                .collect { user ->
                    _currentUser.value = user
                    _isSeller.value = user?.isSeller == true

                    if (user != null) {
                        Log.d(TAG, "👤 Usuario: ${user.email}, seller: ${user.isSeller}")
                        if (_isOnline.value) {
                            startFirebaseSync()
                            syncCartUseCase.syncFromFirebase()
                        }
                        observeCartCount()
                        startNotificationPolling()
                    } else {
                        Log.d(TAG, "👤 Sesión cerrada")
                        stopFirebaseSync()
                        stopNotificationPolling()
                        _cartItemCount.value = 0
                        _unreadNotifications.value = 0
                    }
                }
        }
    }

    private fun observeCartCount() {
        viewModelScope.launch {
            getCartItemsUseCase()
                .catch { e ->
                    Log.e(TAG, "Error cart count", e)
                    emit(emptyList())
                }
                .collect { items -> _cartItemCount.value = items.sumOf { it.quantity } }
        }
    }

    private fun startNotificationPolling() {
        stopNotificationPolling()
        notificationJob = viewModelScope.launch {
            var lastCount = 0
            while (true) {
                try {
                    val count = getUnreadNotificationCountUseCase()
                    if (count > lastCount) {
                        Log.d(TAG, "🔔 Nueva notificación ($count)")
                        playNotificationSound()
                    }
                    _unreadNotifications.value = count
                    lastCount = count
                } catch (e: Exception) {
                    Log.e(TAG, "Error en notificaciones", e)
                }
                delay(5_000)
            }
        }
    }

    private fun stopNotificationPolling() {
        notificationJob?.cancel()
        notificationJob = null
    }

    fun refreshProducts() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                if (_isOnline.value) {
                    Log.d(TAG, "🔄 Refresh manual desde Firebase")
                    val fresh = getAllClothesUseCase()
                    val entities = fresh.map { ProductEntity.fromDomain(it) }
                    productDao.replaceAllProducts(entities)
                } else {
                    Log.d(TAG, "📴 Offline — Room ya está actualizado")
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
                Log.e(TAG, "Error refresh", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun addToCart(productId: String, productName: String) {
        viewModelScope.launch {
            try {
                addToCartUseCase(productId)
                Log.d(TAG, "🛒 Agregado: $productName")
            } catch (e: Exception) {
                _error.value = "Error al agregar al carrito"
                Log.e(TAG, "Error addToCart", e)
            }
        }
    }

    fun showBecomeSellerDialog() { _showBecomeSellerDialog.value = true }
    fun hideBecomeSellerDialog() { _showBecomeSellerDialog.value = false }

    fun becomeSeller() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            _isLoading.value = true
            try {
                val updated = changeUserRoleUseCase(user.uid, "seller")
                _currentUser.value = updated
                _isSeller.value = true
                _showBecomeSellerDialog.value = false
                if (_isOnline.value) startFirebaseSync()

                // ✅ Obtener token actual y enviar notificación
                val currentToken = notificationHelper.getCurrentFcmToken()
                if (currentToken != null) {
                    notificationHelper.sendNotificationToUser(
                        fcmToken = currentToken,
                        title = "¡Felicidades!",
                        message = "Ahora eres vendedor en Shopy. ¡Comienza a publicar tus productos!"
                    )
                }
            } catch (e: Exception) {
                _error.value = "Error al convertirse en vendedor: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            stopFirebaseSync()
            stopNotificationPolling()
            logoutUseCase()
            _currentUser.value = null
        }
    }

    fun clearError() { _error.value = null }

    override fun onCleared() {
        super.onCleared()
        stopFirebaseSync()
        stopNotificationPolling()
        releaseSound()
    }
}

private fun ProductEntity.toDomain(): Cloth = Cloth(
    id = id,
    name = name,
    description = description,
    size = size,
    price = price,
    stock = stock,
    image = image,
    sellerId = sellerId,
    createdAt = createdAt,
    updatedAt = updatedAt
)