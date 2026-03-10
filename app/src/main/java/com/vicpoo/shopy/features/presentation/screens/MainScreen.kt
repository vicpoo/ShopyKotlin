package com.vicpoo.shopy.features.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vicpoo.shopy.features.presentation.viewmodels.AuthViewModel

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Bienvenido!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Usuario: ${currentUser?.name ?: ""}",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Email: ${currentUser?.email ?: ""}",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Rol: ${currentUser?.role ?: ""}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF2E92)
            )
        ) {
            Text("Cerrar Sesión")
        }
    }
}