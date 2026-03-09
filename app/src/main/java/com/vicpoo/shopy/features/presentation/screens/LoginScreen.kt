//LoginScreen.kt
package com.vicpoo.shopy.features.presentation.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.*

import com.vicpoo.shopy.R
import com.vicpoo.shopy.features.domain.model.LoginRequest
import com.vicpoo.shopy.features.presentation.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onLoginSuccess()
        }
    }
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {

                // Luz rosa izquierda
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.CenterStart)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFF2E92).copy(.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Puntos decorativos
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 15.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (it == 2)
                                        Color(0xFFFF2E92)
                                    else
                                        Color.White.copy(.5f),
                                    CircleShape
                                )
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "S H O P",
                        color = Color.White,
                        fontSize = 34.sp,
                        letterSpacing = 10.sp,
                        fontWeight = FontWeight.Light
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                40.dp,
                                RoundedCornerShape(35.dp),
                                ambientColor = Color(0xFFFF2E92),
                                spotColor = Color(0xFFFF2E92)
                            ),
                        shape = RoundedCornerShape(35.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(.06f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(.15f)
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                "Iniciar Sesión",
                                fontSize = 30.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                "Accede a tu cuenta",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(25.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("Correo electrónico") },
                                leadingIcon = { Icon(Icons.Default.Email, null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color.White.copy(.06f),
                                        RoundedCornerShape(20.dp)
                                    ),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = { Text("Contraseña") },
                                leadingIcon = { Icon(Icons.Default.Lock, null) },
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color.White.copy(.06f),
                                        RoundedCornerShape(20.dp)
                                    ),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(25.dp))

                            Button(
                                onClick = {
                                    authViewModel.login(LoginRequest(email, password))
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

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Text(
                                            "INICIAR SESIÓN",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Icon(
                                            Icons.Default.ArrowForward,
                                            null,
                                            tint = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "¿Olvidaste tu contraseña?",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row {
                                Text("¿No tienes cuenta? ", color = Color.Gray)

                                TextButton(onClick = onNavigateToRegister) {
                                    Text(
                                        "Regístrate",
                                        color = Color(0xFFFF2E92),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
    @Composable
    fun SocialLoginButton(
                        iconRes: Int,
                        text: String,
                        modifier: Modifier = Modifier
                    ) {
                        OutlinedButton(
                            onClick = { /* Handle social login */ },
                            modifier = modifier.height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF222222)
                            ),
                            shape = MaterialTheme.shapes.large,
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = text,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = text,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

