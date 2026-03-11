//FirebaseNotificationRepository.kt
package com.vicpoo.shopy.features.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.vicpoo.shopy.core.firebase.FirebaseConfig
import com.vicpoo.shopy.features.domain.model.Notification
import com.vicpoo.shopy.features.domain.repository.NotificationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseNotificationRepository : NotificationRepository {

    companion object {
        private const val TAG = "NotificationRepo"
    }

    override suspend fun createNotification(notification: Notification): Notification {
        return try {
            val notificationRef = FirebaseConfig.notificationsRef.push()
            val notificationId = notificationRef.key ?: throw Exception("Error al generar ID")

            val notificationMap = HashMap<String, Any>().apply {
                put("title", notification.title)
                put("message", notification.message)
                put("productId", notification.productId ?: "")
                put("timestamp", System.currentTimeMillis())
                put("read", false)
            }

            notificationRef.setValue(notificationMap).await()
            Log.d(TAG, "Notificación creada con ID: $notificationId")

            notification.copy(
                id = notificationId,
                timestamp = System.currentTimeMillis(),
                read = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear notificación", e)
            throw Exception("Error al crear notificación: ${e.message}")
        }
    }

    override fun getNotifications(): Flow<List<Notification>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val notifications = snapshot.children.mapNotNull { dataSnapshot ->
                        val id = dataSnapshot.key ?: return@mapNotNull null
                        val value = dataSnapshot.value as? Map<String, Any> ?: return@mapNotNull null

                        Notification(
                            id = id,
                            title = value["title"] as? String ?: return@mapNotNull null,
                            message = value["message"] as? String ?: return@mapNotNull null,
                            productId = value["productId"] as? String,
                            timestamp = (value["timestamp"] as? Long) ?: 0,
                            read = (value["read"] as? Boolean) ?: false
                        )
                    }.sortedByDescending { it.timestamp }

                    trySend(notifications).isSuccess
                } catch (e: Exception) {
                    Log.e(TAG, "Error al procesar notificaciones", e)
                    trySend(emptyList()).isSuccess
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error en listener: ${error.message}")
                // No cerramos el flow, solo enviamos lista vacía
                trySend(emptyList()).isSuccess
            }
        }

        FirebaseConfig.notificationsRef.addValueEventListener(listener)
        awaitClose {
            try {
                FirebaseConfig.notificationsRef.removeEventListener(listener)
            } catch (e: Exception) {
                Log.e(TAG, "Error al remover listener", e)
            }
        }
    }

    override suspend fun markAsRead(notificationId: String) {
        try {
            Log.d(TAG, "Marcando como leída: $notificationId")
            FirebaseConfig.notificationsRef.child(notificationId)
                .child("read")
                .setValue(true)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error al marcar como leída", e)
            // No lanzamos excepción para no bloquear la UI
        }
    }

    override suspend fun markAllAsRead() {
        try {
            Log.d(TAG, "Marcando todas como leídas")
            val snapshot = FirebaseConfig.notificationsRef.get().await()
            snapshot.children.forEach { child ->
                child.ref.child("read").setValue(true).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al marcar todas como leídas", e)
        }
    }

    override suspend fun deleteNotification(notificationId: String) {
        try {
            Log.d(TAG, "Eliminando notificación: $notificationId")
            FirebaseConfig.notificationsRef.child(notificationId).removeValue().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar notificación", e)
        }
    }

    override suspend fun getUnreadCount(): Int {
        return try {
            val snapshot = FirebaseConfig.notificationsRef
                .orderByChild("read")
                .equalTo(false)
                .get()
                .await()
            snapshot.children.count()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener contador", e)
            0
        }
    }
}