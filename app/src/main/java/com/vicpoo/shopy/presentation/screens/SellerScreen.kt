package com.vicpoo.shopy.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vicpoo.shopy.core.utils.Base64ImageUtils
import com.vicpoo.shopy.domain.model.Cloth
import com.vicpoo.shopy.presentation.components.ClothDialog
import com.vicpoo.shopy.presentation.viewmodels.SellerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: SellerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val clothes by viewModel.sellerClothes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCloth by remember { mutableStateOf<Cloth?>(null) }

    LaunchedEffect(error) {
        error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Mis Productos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar",
                        tint = Color(0xFFFF2E92)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = clothes.size.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF2E92)
                        )
                        Text(
                            text = "Total",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = clothes.count { it.stock ?: 0 > 0 }.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Green
                        )
                        Text(
                            text = "En stock",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = clothes.count { it.stock ?: 0 == 0 }.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Text(
                            text = "Agotados",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (clothes.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No tienes productos aún",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF2E92)
                            )
                        ) {
                            Text("Agregar tu primer producto")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(clothes) { cloth ->
                        SellerProductItem(
                            cloth = cloth,
                            onEdit = { selectedCloth = cloth },
                            onDelete = { viewModel.deleteCloth(cloth.id) }
                        )
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFFF2E92)
                )
            }
        }
    }

    if (showAddDialog || selectedCloth != null) {
        ClothDialog(
            title = if (selectedCloth == null) "Agregar Producto" else "Editar Producto",
            cloth = selectedCloth,
            onDismiss = {
                showAddDialog = false
                selectedCloth = null
            },
            onSave = { clothRequest, imageFile ->
                if (selectedCloth == null) {
                    val newCloth = Cloth(
                        name = clothRequest.name,
                        description = clothRequest.description,
                        size = clothRequest.size,
                        price = clothRequest.price,
                        stock = clothRequest.stock,
                        image = clothRequest.image,
                        sellerId = currentUser?.uid ?: ""
                    )
                    viewModel.createCloth(newCloth, imageFile)
                } else {
                    val updatedCloth = selectedCloth!!.copy(
                        name = clothRequest.name,
                        description = clothRequest.description,
                        size = clothRequest.size,
                        price = clothRequest.price,
                        stock = clothRequest.stock,
                        image = clothRequest.image
                    )
                    viewModel.updateCloth(updatedCloth, imageFile)
                }
                showAddDialog = false
                selectedCloth = null
            }
        )
    }
}

@Composable
fun SellerProductItem(
    cloth: Cloth,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }

    LaunchedEffect(cloth.image) {
        if (cloth.image != null && cloth.image!!.isNotEmpty()) {
            isLoadingImage = true
            try {
                if (cloth.image!!.startsWith("/9j/") || cloth.image!!.length > 100) {
                    val bitmap = Base64ImageUtils.base64ToBitmap(cloth.image!!)
                    imageBitmap = bitmap?.asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingImage = false
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoadingImage) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFF2E92),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(30.dp)
                    )
                }
            } else if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = cloth.name,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cloth.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Text(
                    text = "$${cloth.price ?: 0}",
                    fontSize = 14.sp,
                    color = Color(0xFFFF2E92)
                )

                Text(
                    text = "Stock: ${cloth.stock ?: 0}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (!cloth.size.isNullOrBlank()) {
                    Text(
                        text = "Tallas: ${cloth.size}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                if (cloth.image != null && cloth.image!!.isNotEmpty()) {
                    val sizeKB = Base64ImageUtils.getBase64SizeInKB(cloth.image!!)
                    Text(
                        text = "Imagen: ${sizeKB}KB",
                        fontSize = 10.sp,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFFFF2E92)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}