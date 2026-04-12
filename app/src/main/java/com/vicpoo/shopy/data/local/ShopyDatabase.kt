// data/local/ShopyDatabase.kt
package com.vicpoo.shopy.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vicpoo.shopy.data.local.converter.Converters
import com.vicpoo.shopy.data.local.dao.CartDao
import com.vicpoo.shopy.data.local.dao.PendingOperationDao
import com.vicpoo.shopy.data.local.dao.ProductDao
import com.vicpoo.shopy.data.local.entity.CartItemEntity
import com.vicpoo.shopy.data.local.entity.PendingOperationEntity
import com.vicpoo.shopy.data.local.entity.ProductEntity

@Database(
    entities = [
        CartItemEntity::class,
        ProductEntity::class,
        PendingOperationEntity::class  // ← Asegúrate que esté incluida
    ],
    version = 2,  // ← Incrementa la versión si ya existía
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ShopyDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun productDao(): ProductDao
    abstract fun pendingOperationDao(): PendingOperationDao

    companion object {
        @Volatile
        private var INSTANCE: ShopyDatabase? = null

        fun getInstance(): ShopyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    com.vicpoo.shopy.ShopyApplication.instance,
                    ShopyDatabase::class.java,
                    "shopy_database"
                )
                    .fallbackToDestructiveMigration()  // Para desarrollo, luego cambiar a migración
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}