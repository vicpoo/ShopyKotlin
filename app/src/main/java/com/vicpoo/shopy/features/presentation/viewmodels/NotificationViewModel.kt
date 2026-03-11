//NotificationViewModel.kt
package com.vicpoo.shopy.features.presentation.viewmodels

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicpoo.shopy.R
import com.vicpoo.shopy.features.domain.model.Notification
import com.vicpoo.shopy.features.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val createNotificationUseCase: CreateNotificationUseCase,
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markAsReadUseCase: MarkNotificationAsReadUseCase,
    private val markAllAsReadUseCase: MarkAllNotificationsAsReadUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase,
    private val getUnreadCountUseCase: GetUnreadNotificationCountUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "NotificationVM"
    }

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var soundPool: SoundPool? = null
    private var notificationSoundId: Int = 0
    private var isSoundLoaded = false

    init {
        observeNotifications()
        loadUnreadCount()
    }

    fun initSound(context: Context) {
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
                }
            }

            notificationSoundId = soundPool?.load(context, R.raw.notification_sound, 1) ?: 0
            Log.d(TAG, "Sound ID: $notificationSoundId")
        } catch (e: Exception) {
            Log.e(TAG, "Error initSound", e)
        }
    }

    fun playNotificationSound() {
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

    fun releaseSound() {
        try {
            soundPool?.release()
            soundPool = null
            isSoundLoaded = false
            Log.d(TAG, "SoundPool liberado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al liberar SoundPool", e)
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            getNotificationsUseCase().collect { notificationsList ->
                val oldCount = _notifications.value.size
                _notifications.value = notificationsList
                loadUnreadCount()

                // Si hay una notificación nueva, reproducir sonido
                if (notificationsList.size > oldCount) {
                    Log.d(TAG, "Nueva notificación detectada")
                    playNotificationSound()
                }
            }
        }
    }

    fun createNotification(notification: Notification) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val created = createNotificationUseCase(notification)
                Log.d(TAG, "Notificación creada: ${created.id}")
                // El sonido se reproducirá automáticamente en observeNotifications
            } catch (e: Exception) {
                Log.e(TAG, "Error al crear notificación", e)
                _error.value = "Error al crear notificación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                markAsReadUseCase(notificationId)
                Log.d(TAG, "Marcada como leída: $notificationId")
            } catch (e: Exception) {
                Log.e(TAG, "Error al marcar como leída", e)
                _error.value = "Error al marcar como leída: ${e.message}"
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                markAllAsReadUseCase()
                Log.d(TAG, "Todas marcadas como leídas")
            } catch (e: Exception) {
                Log.e(TAG, "Error al marcar todas como leídas", e)
                _error.value = "Error al marcar todas como leídas: ${e.message}"
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                deleteNotificationUseCase(notificationId)
                Log.d(TAG, "Eliminada: $notificationId")
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar notificación", e)
                _error.value = "Error al eliminar notificación: ${e.message}"
            }
        }
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                _unreadCount.value = getUnreadCountUseCase()
                Log.d(TAG, "Notificaciones no leídas: ${_unreadCount.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar contador", e)
            }
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