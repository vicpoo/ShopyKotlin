//SellerProductDetailScreen.kt
package com.vicpoo.shopy.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vicpoo.shopy.core.utils.Base64ImageUtils
import com.vicpoo.shopy.domain.model.Review
import com.vicpoo.shopy.presentation.components.RatingBar
import com.vicpoo.shopy.presentation.components.ReviewItem
import com.vicpoo.shopy.presentation.viewmodels.SellerProductDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProductDetailScreen(
    navController: NavController,
    productId: String,
    onBack: () -> Unit,
    viewModel: SellerProductDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val product by viewModel.product.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val showDeleteDialog by viewModel.showDeleteConfirmDialog.collectAsState()
    val reviewToDelete by viewModel.reviewToDelete.collectAsState()

    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    LaunchedEffect(product?.image) {
        if (product?.image != null && product!!.image!!.isNotEmpty()) {
            try {
                val bitmap = Base64ImageUtils.base64ToBitmap(product!!.image!!)
                imageBitmap = bitmap?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            androidx.compose.material3.SnackbarHostState().showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF050505), Color(0xFF0B0B0F))
                )
            )
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con imagen
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Black)
                ) {
                    if (imageBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = imageBitmap!!,
                            contentDescription = product?.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    // Botón de retroceso
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                RoundedCornerShape(28.dp)
                            )
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                }
            }

            // Información del producto
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = product?.name ?: "",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating promedio
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RatingBar(
                                rating = product?.averageRating ?: 0.0,
                                starSize = 20.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${product?.totalReviews ?: 0} reseñas)",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Precio: $${String.format("%.2f", product?.price ?: 0.0)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF2E92)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (!product?.size.isNullOrBlank()) {
                                Text(
                                    text = "Tallas: ${product?.size}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = "Stock: ${product?.stock ?: 0}",
                                fontSize = 14.sp,
                                color = if ((product?.stock ?: 0) > 0) Color.Green else Color.Red
                            )
                        }

                        if (product?.description != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Descripción",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product?.description ?: "",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Sección de reseñas
            item {
                Text(
                    text = "Opiniones de clientes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Lista de reseñas
            if (reviews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Este producto aún no tiene reseñas",
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(reviews) { review ->
                    ReviewItem(
                        review = review,
                        isUserReview = false,
                        showDeleteButton = true,
                        onDelete = {
                            viewModel.confirmDeleteReview(review)
                        }
                    )
                }
            }
        }

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF2E92))
            }
        }
    }

    // Dialog de confirmación de eliminación
    if (showDeleteDialog && reviewToDelete != null) {
        Dialog(onDismissRequest = { viewModel.hideDeleteConfirmDialog() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1C1C1E)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF2E92),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Eliminar reseña",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "¿Estás seguro de que quieres eliminar esta reseña? Esta acción no se puede deshacer.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.hideDeleteConfirmDialog() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Gray
                            )
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = { viewModel.deleteReview() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF2E92)
                            )
                        ) {
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }
}