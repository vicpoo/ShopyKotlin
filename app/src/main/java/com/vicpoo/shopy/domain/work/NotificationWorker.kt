// domain/work/NotificationWorker.kt
package com.vicpoo.shopy.domain.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "NotificationWorker"
        const val WORK_NAME = "notification_work"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "🔔 WorkManager: Sincronizando tokens FCM")

            // Obtener token actual
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "📱 FCM Token: ${token.take(20)}...")

            // Guardar token asociado al usuario actual
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val userFcmRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("fcmTokens")
                    .child(token)

                userFcmRef.setValue(mapOf(
                    "token" to token,
                    "device" to android.os.Build.MODEL,
                    "timestamp" to System.currentTimeMillis()
                )).await()

                Log.d(TAG, "✅ Token guardado para usuario: $userId")
            }

            // Suscribir a topic global
            FirebaseMessaging.getInstance().subscribeToTopic("all_users").await()
            Log.d(TAG, "✅ Suscrito a topic: all_users")

            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en NotificationWorker", e)
            Result.retry()
        }
    }
}