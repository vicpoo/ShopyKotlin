//LoginScreen.kt
package com.vicpoo.shopy.presentation.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.vicpoo.shopy.R
import com.vicpoo.shopy.domain.model.LoginRequest
import com.vicpoo.shopy.presentation.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val showCaptcha by viewModel.showCaptcha.collectAsStateWithLifecycle()

    var passwordVisible by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { idToken ->
                viewModel.loginWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Error con Google Sign-In", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onLoginSuccess()
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
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.CenterStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF2E92).copy(.35f),
                            Color.Transparent
                        )
                    )
                )
                .blur(120.dp)
        )

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
                text = "S H O P Y",
                color = Color.White,
                fontSize = 34.sp,
                letterSpacing = 12.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        50.dp,
                        RoundedCornerShape(35.dp),
                        ambientColor = Color(0xFFFF2E92),
                        spotColor = Color(0xFFFF2E92)
                    ),
                shape = RoundedCornerShape(35.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1C1C1E).copy(alpha = 0.85f)
                ),
                border = BorderStroke(
                    1.dp,
                    Color.White.copy(.08f)
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
                                Color.White.copy(.08f),
                                RoundedCornerShape(20.dp)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFF2E92)
                        ),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
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
                                Color.White.copy(.08f),
                                RoundedCornerShape(20.dp)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFF2E92)
                        ),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(25.dp))

                    Button(
                        onClick = {
                            when {
                                email.isBlank() || password.isBlank() -> {
                                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    // Usar requestLogin en lugar de login directo
                                    viewModel.requestLogin(email, password)
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
                                            Color(0xFFFF4FA3)
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
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row {
                        Text("¿No tienes cuenta? ", color = Color.Gray)

                        TextButton(
                            onClick = onNavigateToRegister,
                            enabled = !isLoading
                        ) {
                            Text(
                                "Regístrate",
                                color = Color(0xFFFF2E92),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        Text(
                            " O ",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Divider(
                            modifier = Modifier.weight(1f),
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(context.getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build()

                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                        enabled = !isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                "Continuar con Google",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo del captcha
    if (showCaptcha) {
        CaptchaScreen(
            onSuccess = {
                viewModel.proceedAfterCaptcha()
            },
            onDismiss = {
                viewModel.hideCaptcha()
            }
        )
    }
}