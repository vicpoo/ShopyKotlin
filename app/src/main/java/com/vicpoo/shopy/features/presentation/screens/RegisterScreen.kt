//RegisterScreen.kt
package com.vicpoo.shopy.features.presentation.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .shadow(20.dp, spotColor = Color(0xFFFF2E92), shape = CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFF2E92), Color(0xFFF5F5F5))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = "Registro",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CREAR CUENTA",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Únete a nuestra comunidad de moda",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = {
                                Text(
                                    "Nombre",
                                    color = Color(0xFF666666)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Nombre",
                                    tint = Color(0xFFFF2E92)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2E92),
                                unfocusedBorderColor = Color(0xFFDDDDDD),
                                focusedLabelColor = Color(0xFFFF2E92),
                                unfocusedLabelColor = Color(0xFF666666)
                            ),
                            shape = MaterialTheme.shapes.large
                        )

                        OutlinedTextField(
                            value = lastname,
                            onValueChange = { lastname = it },
                            label = {
                                Text(
                                    "Apellido",
                                    color = Color(0xFF666666)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.PersonOutline,
                                    contentDescription = "Apellido",
                                    tint = Color(0xFFFF2E92)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2E92),
                                unfocusedBorderColor = Color(0xFFDDDDDD),
                                focusedLabelColor = Color(0xFFFF2E92),
                                unfocusedLabelColor = Color(0xFF666666)
                            ),
                            shape = MaterialTheme.shapes.large
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = {
                            Text(
                                "Correo electrónico",
                                color = Color(0xFF666666)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color(0xFFFF2E92)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF2E92),
                            unfocusedBorderColor = Color(0xFFDDDDDD),
                            focusedLabelColor = Color(0xFFFF2E92),
                            unfocusedLabelColor = Color(0xFF666666)
                        ),
                        shape = MaterialTheme.shapes.large
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = {
                            Text(
                                "Contraseña",
                                color = Color(0xFF666666)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = Color(0xFFFF2E92)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF2E92),
                            unfocusedBorderColor = Color(0xFFDDDDDD),
                            focusedLabelColor = Color(0xFFFF2E92),
                            unfocusedLabelColor = Color(0xFF666666)
                        ),
                        shape = MaterialTheme.shapes.large
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = {
                            Text(
                                "Confirmar contraseña",
                                color = Color(0xFF666666)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Confirm Password",
                                tint = Color(0xFFFF2E92)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF2E92),
                            unfocusedBorderColor = Color(0xFFDDDDDD),
                            focusedLabelColor = Color(0xFFFF2E92),
                            unfocusedLabelColor = Color(0xFF666666)
                        ),
                        shape = MaterialTheme.shapes.large
                    )

                    // Terms and conditions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = true,
                            onCheckedChange = { /* Handle checkbox */ },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF2E92),
                                uncheckedColor = Color(0xFFDDDDDD)
                            )
                        )
                        Text(
                            text = "Acepto los términos y condiciones",
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (password == confirmPassword && email.isNotEmpty()) {
                                authViewModel.register(
                                    RegisterRequest(email, password, name, lastname)
                                )
                            } else if (password != confirmPassword) {
                                showToast(context, "Las contraseñas no coinciden")
                            }
                        },
                        enabled = email.isNotEmpty() && password.isNotEmpty() &&
                                confirmPassword.isNotEmpty() && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF2E92)
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                "CREAR CUENTA",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            onNavigateToLogin()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "¿Ya tienes cuenta? Inicia sesión aquí",
                            color = Color(0xFFFF2E92),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}