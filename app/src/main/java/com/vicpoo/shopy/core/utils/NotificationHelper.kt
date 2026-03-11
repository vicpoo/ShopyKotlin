package com.vicpoo.shopy.core.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object NotificationHelper {

    private const val TAG = "NotificationHelper"

    // Función para enviar notificación a todos los usuarios (simulada)
    // En producción, necesitarías un servidor backend para esto
    suspend fun sendNotificationToAllUsers(title: String, message: String, productId: String? = null) {
        try {
            // Obtener token de FCM
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM Token: $token")

            // Aquí normalmente enviarías el token a tu servidor
            // y el servidor enviaría la notificación a todos los dispositivos

            // Como estamos en desarrollo, la notificación se maneja
            // a través de Realtime Database y se muestra localmente
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener token FCM", e)
        }
    }
}