//NotificationUseCases.kt
package com.vicpoo.shopy.features.domain.usecase

import com.vicpoo.shopy.features.domain.model.Notification
import com.vicpoo.shopy.features.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class CreateNotificationUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(notification: Notification): Notification =
        repository.createNotification(notification)
}

class GetNotificationsUseCase(private val repository: NotificationRepository) {
    operator fun invoke(): Flow<List<Notification>> = repository.getNotifications()
}

class MarkNotificationAsReadUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(notificationId: String) = repository.markAsRead(notificationId)
}

class MarkAllNotificationsAsReadUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke() = repository.markAllAsRead()
}

class DeleteNotificationUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(notificationId: String) = repository.deleteNotification(notificationId)
}

class GetUnreadNotificationCountUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(): Int = repository.getUnreadCount()
}