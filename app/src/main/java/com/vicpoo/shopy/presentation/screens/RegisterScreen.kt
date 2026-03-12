package com.vicpoo.shopy.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vicpoo.shopy.domain.model.RegisterRequest
import com.vicpoo.shopy.presentation.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onRegisterSuccess()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
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
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            val path = Path().apply {

                moveTo(size.width * 0.9f, size.height * 0.1f)

                cubicTo(
                    size.width * 1.1f, size.height * 0.3f,
                    size.width * 0.7f, size.height * 0.5f,
                    size.width * 0.95f, size.height * 0.8f
                )
            }

            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFFF2E92),
                        Color(0xFFFF6AA6)
                    )
                ),
                style = Stroke(
                    width = 6f,
                    cap = StrokeCap.Round
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "SHOPY",
                color = Color.White,
                fontSize = 34.sp,
                letterSpacing = 8.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Crear Cuenta",
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Text(
                text = "Únete a nuestra tienda",
                color = Color(0xFFB3B3B3),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(30.dp))

            Box {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .blur(40.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0x22FF2E92),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .blur(0.dp),

                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.04f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.08f)
                    )

                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nombre completo") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2E92),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFFFF2E92)
                            ),
                            enabled = !isLoading
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Correo electrónico") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2E92),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFFFF2E92)
                            ),
                            enabled = !isLoading
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Contraseña") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2E92),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFFFF2E92)
                            ),
                            enabled = !isLoading
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Confirmar contraseña") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
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
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2E92),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFFFF2E92)
                            ),
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                when {
                                    email.isBlank() || password.isBlank() || name.isBlank() -> {
                                        Toast.makeText(
                                            context,
                                            "Completa todos los campos",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    password != confirmPassword -> {
                                        Toast.makeText(
                                            context,
                                            "Las contraseñas no coinciden",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    password.length < 6 -> {
                                        Toast.makeText(
                                            context,
                                            "La contraseña debe tener al menos 6 caracteres",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    else -> {
                                        viewModel.register(
                                            RegisterRequest(
                                                email = email,
                                                password = password,
                                                name = name,
                                                role = "user"
                                            )
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(),
                            enabled = !isLoading
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
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        "CREAR CUENTA",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = onNavigateToLogin,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
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
}