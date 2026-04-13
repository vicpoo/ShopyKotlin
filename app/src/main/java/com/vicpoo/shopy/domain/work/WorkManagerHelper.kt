// domain/work/WorkManagerHelper.kt (versión alternativa más simple)
package com.vicpoo.shopy.domain.work

import android.content.Context
import android.util.Log
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "WorkManagerHelper"

        private const val SYNC_INTERVAL_MINUTES = 15L
        private const val CLEANUP_INTERVAL_HOURS = 24L
        private const val NOTIFICATION_INTERVAL_HOURS = 24L
        private const val SYNC_FLEX_MINUTES = 5L
        private const val CLEANUP_FLEX_HOURS = 2L
        private const val NOTIFICATION_FLEX_HOURS = 2L
    }

    private val workManager = WorkManager.getInstance(context)

    fun scheduleAllWork() {
        scheduleCartSyncWork()
        scheduleCleanupWork()
        scheduleNotificationWork()
        Log.d(TAG, "✅ Todos los workers programados")
    }

    fun scheduleCartSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncCartWorker>(
            SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES,
            SYNC_FLEX_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                1, TimeUnit.MINUTES
            )
            .addTag(SyncCartWorker.WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncCartWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        Log.d(TAG, "📅 WorkManager: Sincronización de carrito programada cada $SYNC_INTERVAL_MINUTES minutos")
    }

    fun scheduleCleanupWork() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val cleanupRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
            CLEANUP_INTERVAL_HOURS, TimeUnit.HOURS,
            CLEANUP_FLEX_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(CleanupWorker.WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            CleanupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )

        Log.d(TAG, "🧹 WorkManager: Limpieza programada cada $CLEANUP_INTERVAL_HOURS horas")
    }

    fun scheduleNotificationWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val notificationRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            NOTIFICATION_INTERVAL_HOURS, TimeUnit.HOURS,
            NOTIFICATION_FLEX_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(NotificationWorker.WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            NotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            notificationRequest
        )

        Log.d(TAG, "🔔 WorkManager: Notificaciones programadas cada $NOTIFICATION_INTERVAL_HOURS horas")
    }

    fun syncNow() {
        CoroutineScope(Dispatchers.IO).launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncCartWorker>()
                .setConstraints(constraints)
                .addTag(SyncCartWorker.WORK_NAME)
                .build()

            workManager.enqueue(syncRequest)
            Log.d(TAG, "⚡ WorkManager: Sincronización inmediata solicitada")
        }
    }

    fun cancelAllWork() {
        workManager.cancelAllWork()
        Log.d(TAG, "🛑 WorkManager: Todos los workers cancelados")
    }

    // Método simplificado - eliminamos getWorkStatus para evitar errores
}