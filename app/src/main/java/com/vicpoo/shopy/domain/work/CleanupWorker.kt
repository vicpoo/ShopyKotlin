// domain/work/CleanupWorker.kt
package com.vicpoo.shopy.domain.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vicpoo.shopy.data.local.dao.ProductDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CleanupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val productDao: ProductDao
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "CleanupWorker"
        const val WORK_NAME = "cleanup_work"

        // 7 días en milisegundos
        private const val SEVEN_DAYS_IN_MS = 7 * 24 * 60 * 60 * 1000L
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🧹 WorkManager: Limpiando productos antiguos")

            val sevenDaysAgo = System.currentTimeMillis() - SEVEN_DAYS_IN_MS

            // Obtener productos antiguos
            val allProducts = productDao.getAllProductsOnce()
            val oldProducts = allProducts.filter { it.lastSynced < sevenDaysAgo }

            if (oldProducts.isNotEmpty()) {
                Log.d(TAG, "🧹 Eliminando ${oldProducts.size} productos antiguos")
                // Aquí puedes implementar la lógica de eliminación
                // productDao.deleteProducts(oldProducts.map { it.id })
            } else {
                Log.d(TAG, "🧹 No hay productos antiguos para limpiar")
            }

            Log.d(TAG, "✅ WorkManager: Limpieza completada")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "❌ WorkManager: Error en limpieza", e)
            Result.retry()
        }
    }
}