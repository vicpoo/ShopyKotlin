//MainActivity.kt
package com.vicpoo.shopy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.vicpoo.shopy.navigation.AppNavigation
import com.vicpoo.shopy.ui.theme.ShopyTheme
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var navController: NavController
    private var pendingProductId: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No recibirás notificaciones push", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permiso de notificaciones para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_DENIED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Verificar si la app fue abierta desde una notificación
        intent?.let {
            handleNotificationIntent(it)
        }

        setContent {
            ShopyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    // ✅ CORREGIDO: onNewIntent con Intent no nullable
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    // ✅ CORREGIDO: handleNotificationIntent recibe Intent no nullable
    private fun handleNotificationIntent(intent: Intent) {
        if (intent.getBooleanExtra("from_notification", false)) {
            val productId = intent.getStringExtra("product_id")
            if (productId != null) {
                Log.d("MainActivity", "🔔 Abriendo producto desde notificación: $productId")
                pendingProductId = productId
                // La navegación se manejará cuando la UI esté lista
                // Puedes usar un canal de eventos o StateFlow para comunicarte con la navegación
            }
        }
    }

    fun getPendingProductId(): String? {
        val id = pendingProductId
        pendingProductId = null
        return id
    }
}