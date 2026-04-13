package com.vicpoo.shopy.core.utils

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "NotificationHelper"
    }

    private val client = OkHttpClient()
    private var accessToken: String? = null
    private var tokenExpiryTime: Long = 0
    private var projectId: String? = null
    private var credentials: GoogleCredentials? = null

    // ✅ AHORA CON withContext(Dispatchers.IO)
    private suspend fun initCredentials(): Boolean = withContext(Dispatchers.IO) {
        if (credentials != null && projectId != null) {
            return@withContext true
        }

        return@withContext try {
            val resourceId = context.resources.getIdentifier("shopi_192605", "raw", context.packageName)
            val inputStream: InputStream = context.resources.openRawResource(resourceId)

            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            projectId = jsonObject.getString("project_id")

            val credStream = context.resources.openRawResource(resourceId)
            credentials = GoogleCredentials.fromStream(credStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            credStream.close()

            credentials?.refreshIfExpired()
            accessToken = credentials?.accessToken?.tokenValue
            tokenExpiryTime = System.currentTimeMillis() + 3500000

            Log.d(TAG, "✅ Credenciales FCM inicializadas. Project ID: $projectId")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inicializando credenciales FCM", e)
            false
        }
    }

    private suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return@withContext accessToken
        }

        if (credentials == null) {
            val initialized = initCredentials()
            if (!initialized) return@withContext null
        }

        return@withContext try {
            credentials?.refreshIfExpired()
            val token = credentials?.accessToken?.tokenValue

            accessToken = token
            tokenExpiryTime = System.currentTimeMillis() + 3500000

            Log.d(TAG, "✅ Token OAuth2 renovado: ${token?.take(20)}...")
            token

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo token OAuth2", e)
            null
        }
    }

    suspend fun sendNotificationToUser(
        fcmToken: String,
        title: String,
        message: String,
        productId: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (fcmToken.isEmpty()) {
            Log.e(TAG, "❌ Token FCM vacío")
            return@withContext false
        }

        val token = getAccessToken()
        if (token == null) {
            Log.e(TAG, "❌ No se pudo obtener token de acceso")
            return@withContext false
        }

        val pid = projectId
        if (pid == null) {
            Log.e(TAG, "❌ Project ID no disponible")
            return@withContext false
        }

        return@withContext try {
            val json = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", fcmToken)

                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", message)
                    })

                    put("android", JSONObject().apply {
                        put("priority", "high")
                        put("notification", JSONObject().apply {
                            put("sound", "default")
                            put("channel_id", "shopy_notifications")
                        })
                    })

                    put("data", JSONObject().apply {
                        put("title", title)
                        put("message", message)
                        productId?.let { put("productId", it) }
                    })
                })
            }

            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://fcm.googleapis.com/v1/projects/$pid/messages:send")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful

            if (success) {
                Log.d(TAG, "✅ Notificación enviada a usuario específico")
            } else {
                Log.e(TAG, "❌ Error FCM: ${response.body?.string()}")
            }

            response.close()
            success

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando notificación a usuario", e)
            false
        }
    }

    suspend fun sendNotificationToAllUsers(
        title: String,
        message: String,
        productId: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val token = getAccessToken()
        if (token == null) {
            Log.e(TAG, "❌ No se pudo obtener token de acceso")
            return@withContext false
        }

        val pid = projectId
        if (pid == null) {
            Log.e(TAG, "❌ Project ID no disponible")
            return@withContext false
        }

        return@withContext try {
            val json = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("topic", "all_users")

                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", message)
                    })

                    put("android", JSONObject().apply {
                        put("priority", "high")
                        put("notification", JSONObject().apply {
                            put("sound", "default")
                            put("channel_id", "shopy_notifications")
                        })
                    })

                    put("data", JSONObject().apply {
                        put("title", title)
                        put("message", message)
                        productId?.let { put("productId", it) }
                    })
                })
            }

            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://fcm.googleapis.com/v1/projects/$pid/messages:send")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful

            if (success) {
                Log.d(TAG, "✅ Notificación enviada a all_users")
            } else {
                Log.e(TAG, "❌ Error FCM: ${response.body?.string()}")
            }

            response.close()
            success

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando notificación", e)
            false
        }
    }

    suspend fun subscribeToTopic(topic: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            Log.d(TAG, "✅ Suscrito a topic: $topic")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error suscribiendo a topic: $topic", e)
            false
        }
    }

    suspend fun getCurrentFcmToken(): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "📱 Token FCM: ${token.take(20)}...")
            token
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo token", e)
            null
        }
    }
}