// di/DatabaseModule.kt
package com.vicpoo.shopy.di

import android.content.Context
import androidx.room.Room
import com.vicpoo.shopy.data.local.ShopyDatabase
import com.vicpoo.shopy.data.local.dao.CartDao
import com.vicpoo.shopy.data.local.dao.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ShopyDatabase {
        return Room.databaseBuilder(
            context,
            ShopyDatabase::class.java,
            "shopy_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCartDao(database: ShopyDatabase): CartDao = database.cartDao()

    @Provides
    fun provideProductDao(database: ShopyDatabase): ProductDao = database.productDao()
}