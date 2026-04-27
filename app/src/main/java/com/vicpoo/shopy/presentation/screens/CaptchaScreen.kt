// CaptchaScreen.kt
package com.vicpoo.shopy.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle   // ✅ FIX: CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage              // ✅ FIX: Image → AsyncImage (o usa el de abajo)
import androidx.compose.foundation.Image   // ✅ FIX alternativo si usas painterResource
import com.vicpoo.shopy.presentation.viewmodels.CaptchaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptchaScreen(
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: CaptchaViewModel = hiltViewModel()
) {
    val images by viewModel.images.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val isValidated by viewModel.isValidated.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadCaptcha()
    }

    LaunchedEffect(isValidated) {
        if (isValidated == true) {
            onSuccess()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1C1C1E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verificación de seguridad",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Selecciona todas las imágenes que contengan el mismo objeto",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Grid 2x2
                if (images.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(images) { image ->
                            CaptchaImageItem(
                                image = image,
                                isSelected = selected.contains(image.id),
                                onClick = { viewModel.toggleSelection(image.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.loadCaptcha() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        // ✅ FIX: OutlinedButtonDefaults no existe en M3, se usa colors así:
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevas imágenes")
                    }

                    Button(
                        onClick = { viewModel.validate() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF2E92)
                        )
                    ) {
                        Text("Verificar", color = Color.White)
                    }
                }

                // Mensaje de error
                if (isValidated == false) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF2E92).copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "❌ Selección incorrecta. Intenta de nuevo.",
                            color = Color(0xFFFF6AA6),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CaptchaImageItem(
    image: com.vicpoo.shopy.domain.model.CaptchaImage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2E)
        ),
        border = if (isSelected) {
            BorderStroke(3.dp, Color(0xFFFF2E92))
        } else {
            BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ✅ FIX: Image viene de androidx.compose.foundation
            androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = image.resId),
                    contentDescription = "Captcha image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFF2E92).copy(alpha = 0.4f),
                                    Color(0xFFFF6AA6).copy(alpha = 0.4f)
                                )
                            )
                        )
                )

                Icon(
                    // ✅ FIX: CheckCircle importado correctamente
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFFFF2E92),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                )
            }
        }
    }
}