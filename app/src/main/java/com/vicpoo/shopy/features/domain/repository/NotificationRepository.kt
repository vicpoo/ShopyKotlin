//NotificationRepository.kt
package com.vicpoo.shopy.features.domain.repository

import com.vicpoo.shopy.features.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun createNotification(notification: Notification): Notification
    fun getNotifications(): Flow<List<Notification>>
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(notificationId: String)
    suspend fun getUnreadCount(): Int
}