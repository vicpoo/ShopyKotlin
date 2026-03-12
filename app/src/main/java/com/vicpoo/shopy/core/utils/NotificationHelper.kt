//NotificationHelper.kt
package com.vicpoo.shopy.core.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object NotificationHelper {

    private const val TAG = "NotificationHelper"


    suspend fun sendNotificationToAllUsers(title: String, message: String, productId: String? = null) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM Token: $token")

        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener token FCM", e)
        }
    }
}