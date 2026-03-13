//RepositoryModule.kt
package com.vicpoo.shopy.di

import com.vicpoo.shopy.data.repository.*
import com.vicpoo.shopy.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
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
        firebaseCartRepository: FirebaseCartRepository
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        firebaseNotificationRepository: FirebaseNotificationRepository
    ): NotificationRepository
}