// domain/work/NotificationWorker.kt
package com.vicpoo.shopy.domain.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessaging
import com.vicpoo.shopy.domain.repository.NotificationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "NotificationWorker"
        const val WORK_NAME = "notification_work"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔔 WorkManager: Sincronizando notificaciones")

            // Obtener token actual de FCM
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "📱 FCM Token actual: ${token.take(20)}...")

            // Aquí puedes guardar el token en Firebase Database o tu backend
            // saveTokenToDatabase(token)

            // Limpiar notificaciones leídas de más de 30 días
            // cleanupOldNotifications()

            Log.d(TAG, "✅ WorkManager: Notificaciones sincronizadas")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "❌ WorkManager: Error en notificaciones", e)
            Result.retry()
        }
    }
}