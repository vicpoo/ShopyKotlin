//ProductDetailScreen.kt
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vicpoo.shopy.core.utils.Base64ImageUtils
import com.vicpoo.shopy.domain.model.Review
import com.vicpoo.shopy.presentation.components.RatingBar
import com.vicpoo.shopy.presentation.components.ReviewItem
import com.vicpoo.shopy.presentation.viewmodels.ProductDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String,
    onBack: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val product by viewModel.product.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val userReview by viewModel.userReview.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val error by viewModel.error.collectAsState()
    val showDialog by viewModel.showAddReviewDialog.collectAsState()
    val editingReview by viewModel.editingReview.collectAsState()

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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
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

                        if (!product?.size.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tallas disponibles: ${product?.size}",
                                fontSize = 14.sp,
                                color = Color.Gray
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Opiniones de clientes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    if (userReview == null) {
                        Button(
                            onClick = { viewModel.showAddReviewDialog() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF2E92)
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Calificar", fontSize = 12.sp)
                        }
                    }
                }
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
                            text = "Aún no hay reseñas. ¡Sé el primero en calificar!",
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(reviews) { review ->
                    ReviewItem(
                        review = review,
                        isUserReview = review.id == userReview?.id,
                        onEdit = {
                            viewModel.editReview(review)
                        },
                        onDelete = {
                            viewModel.deleteUserReview()
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

    // Dialog para agregar/editar reseña
    if (showDialog) {
        AddReviewDialog(
            initialRating = editingReview?.rating ?: 0,
            initialComment = editingReview?.comment ?: "",
            isEditing = editingReview != null,
            onSubmit = { rating, comment ->
                viewModel.submitReview(rating, comment)
            },
            onDismiss = { viewModel.hideAddReviewDialog() },
            isLoading = isSubmitting
        )
    }
}