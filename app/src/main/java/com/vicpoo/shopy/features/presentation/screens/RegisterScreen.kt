//RegisterScreen.kt
package com.vicpoo.shopy.features.presentation.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vicpoo.shopy.R
import com.vicpoo.shopy.features.domain.model.RegisterRequest
import com.vicpoo.shopy.features.presentation.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onRegisterSuccess()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            showToast(context, it)
            authViewModel.clearError()
        }
    }
    Box(
    modifier = Modifier
        .fillMaxSize()
        .background(
            Brush.verticalGradient(
                listOf(
                    Color(0xFF050505),
                    Color(0xFF0B0B0F),
                    Color(0xFF050505)
                )
            )
        )
    )
        {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = "SHOP",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Crear Cuenta",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Text(
                    text = "Únete en pocos segundos",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(30.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.1f)
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Nombre") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, null)
                                },
                                shape = RoundedCornerShape(20.dp)
                            )

                            OutlinedTextField(
                                value = lastname,
                                onValueChange = { lastname = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Apellido") },
                                leadingIcon = {
                                    Icon(Icons.Default.PersonOutline, null)
                                },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Correo electrónico") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, null)
                            },
                            shape = RoundedCornerShape(20.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Contraseña") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, null)
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    passwordVisible = !passwordVisible
                                }) {

                                    Icon(
                                        if (passwordVisible)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        null
                                    )
                                }
                            },
                            visualTransformation =
                                if (passwordVisible)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                            shape = RoundedCornerShape(20.dp)
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Confirmar contraseña") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, null)
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    confirmPasswordVisible = !confirmPasswordVisible
                                }) {

                                    Icon(
                                        if (confirmPasswordVisible)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        null
                                    )
                                }
                            },
                            visualTransformation =
                                if (confirmPasswordVisible)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                            shape = RoundedCornerShape(20.dp)
                        )

                        Button(
                            onClick = {

                                if (email.isBlank() || password.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Completa todos los campos",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                if (password != confirmPassword) {
                                    Toast.makeText(
                                        context,
                                        "Las contraseñas no coinciden",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                authViewModel.register(
                                    RegisterRequest(
                                        email,
                                        password,
                                        name,
                                        lastname
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues()
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFFF2E92),
                                                Color(0xFFFF6AA6)
                                            )
                                        ),
                                        RoundedCornerShape(30.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {

                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {

                                    Text(
                                        "CREAR CUENTA",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = { onNavigateToLogin() },
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                "¿Ya tienes cuenta? Inicia sesión",
                                color = Color(0xFFFF2E92)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
}

