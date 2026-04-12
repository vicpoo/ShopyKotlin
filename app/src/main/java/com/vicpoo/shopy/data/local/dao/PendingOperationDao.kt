// data/local/dao/PendingOperationDao.kt
package com.vicpoo.shopy.data.local.dao

import androidx.room.*
import com.vicpoo.shopy.data.local.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {
    @Query("SELECT * FROM pending_operations WHERE synced = 0 ORDER BY timestamp ASC")
    fun getPendingOperations(): Flow<List<PendingOperationEntity>>

    @Query("SELECT * FROM pending_operations WHERE synced = 0 ORDER BY timestamp ASC")
    suspend fun getPendingOperationsOnce(): List<PendingOperationEntity>

    @Insert
    suspend fun insertOperation(operation: PendingOperationEntity)

    @Update
    suspend fun updateOperation(operation: PendingOperationEntity)

    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun deleteOperation(id: Long)

    @Query("DELETE FROM pending_operations WHERE synced = 1")
    suspend fun clearSyncedOperations()

    @Query("SELECT COUNT(*) FROM pending_operations WHERE synced = 0")
    suspend fun getPendingCount(): Int
}