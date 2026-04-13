// di/RepositoryModule.kt
package com.vicpoo.shopy.di

import android.content.Context
import com.vicpoo.shopy.core.utils.NotificationHelper
import com.vicpoo.shopy.data.repository.*
import com.vicpoo.shopy.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        firebaseUserRepository: FirebaseUserRepository
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindClothRepository(
        firebaseClothRepository: FirebaseClothRepository
    ): ClothRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        syncCartRepository: SyncCartRepository
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        firebaseNotificationRepository: FirebaseNotificationRepository
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        firebaseReviewRepository: FirebaseReviewRepository
    ): ReviewRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }
}