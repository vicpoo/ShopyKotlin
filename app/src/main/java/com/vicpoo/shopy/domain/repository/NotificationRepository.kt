package com.vicpoo.shopy.domain.repository

import com.vicpoo.shopy.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun createNotification(notification: Notification): Notification
    fun getNotifications(): Flow<List<Notification>>
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(notificationId: String)
    suspend fun getUnreadCount(): Int
}