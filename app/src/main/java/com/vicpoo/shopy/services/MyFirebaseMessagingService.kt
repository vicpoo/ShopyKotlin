//MyFirebaseMessagingService.kt
package com.vicpoo.shopy.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vicpoo.shopy.MainActivity
import com.vicpoo.shopy.R
import com.vicpoo.shopy.data.model.NotificationData
import com.google.firebase.database.FirebaseDatabase

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "shopy_notifications"
        private const val CHANNEL_NAME = "Shopy Notificaciones"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveTokenToDatabase(token)
    }

    private fun saveTokenToDatabase(token: String) {
        val fcmTokensRef = FirebaseDatabase.getInstance().getReference("fcm_tokens")
        val tokenRef = fcmTokensRef.child(token)

        tokenRef.setValue(mapOf(
            "token" to token,
            "timestamp" to System.currentTimeMillis()
        )).addOnSuccessListener {
            android.util.Log.d("FCM", "✅ Token guardado")
        }.addOnFailureListener { e ->
            android.util.Log.e("FCM", "❌ Error: ${e.message}")
        }

        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userFcmRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("fcmTokens")
                .child(token)

            userFcmRef.setValue(mapOf(
                "token" to token,
                "device" to Build.MODEL,
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        android.util.Log.d("FCM", "📨 Mensaje recibido: ${remoteMessage.data}")

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val body = remoteMessage.notification?.body ?: remoteMessage.data["message"]
        val productId = remoteMessage.data["productId"]

        if (title != null && body != null) {
            saveNotificationToDatabase(title, body, productId)
            sendNotification(title, body, productId)
        }
    }

    private fun saveNotificationToDatabase(
        title: String,
        message: String,
        productId: String?
    ) {
        val notificationsRef = FirebaseDatabase.getInstance().getReference("notifications")
        val newNotificationRef = notificationsRef.push()

        val notificationData = NotificationData(
            title = title,
            message = message,
            productId = productId,
            timestamp = System.currentTimeMillis(),
            read = false
        )

        newNotificationRef.setValue(notificationData)
    }

    private fun sendNotification(title: String, message: String, productId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("from_notification", true)
            productId?.let { putExtra("product_id", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de Shopy"
                setSound(soundUri, Notification.AUDIO_ATTRIBUTES_DEFAULT)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        android.util.Log.d("FCM", "✅ Notificación mostrada: $title")
    }
}