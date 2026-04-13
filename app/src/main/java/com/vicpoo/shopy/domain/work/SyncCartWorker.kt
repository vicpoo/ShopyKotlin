// domain/work/SyncCartWorker.kt
package com.vicpoo.shopy.domain.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vicpoo.shopy.data.repository.SyncCartRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncCartWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val syncCartRepository: SyncCartRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "SyncCartWorker"
        const val WORK_NAME = "sync_cart_work"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔄 WorkManager: Iniciando sincronización de carrito")

            // 1. Sincronizar operaciones pendientes (ADD, UPDATE, REMOVE, CLEAR)
            syncCartRepository.trySync()

            // 2. Sincronizar desde Firebase (fusionar cambios)
            syncCartRepository.syncFromFirebase()

            Log.d(TAG, "✅ WorkManager: Sincronización completada")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "❌ WorkManager: Error en sincronización", e)

            // Reintentar con backoff exponencial (1min, 2min, 4min, etc.)
            Result.retry()
        }
    }
}