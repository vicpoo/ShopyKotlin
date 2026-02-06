package com.vicpoo.shopy.features.presentation.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.vicpoo.shopy.R
import com.vicpoo.shopy.features.domain.model.AuthResponse
import com.vicpoo.shopy.features.domain.model.Cloth
import com.vicpoo.shopy.features.presentation.components.ClothDialog
import com.vicpoo.shopy.features.presentation.viewmodels.AuthViewModel
import com.vicpoo.shopy.features.presentation.viewmodels.ClothViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    clothViewModel: ClothViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val clothes by clothViewModel.clothes.collectAsState()
    val isLoading by clothViewModel.isLoading.collectAsState()
    val error by clothViewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCloth by remember { mutableStateOf<Cloth?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let {
            showToast(context, it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF2E92), Color(0xFFFF1493))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Column {
                            Text(
                                text = "SHOP STYLE",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF222222)
                            )
                            currentUser?.let { user ->
                                Text(
                                    text = "Hola, ${user.name ?: user.email}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = { showFilterMenu = !showFilterMenu }
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = Color(0xFFFF2E92)
                        )
                    }
                    IconButton(
                        onClick = { onLogout() }
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = Color(0xFFFF2E92)
                        )
                    }
                },
                modifier = Modifier.shadow(elevation = 4.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFFF2E92),
                contentColor = Color.White,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, shape = CircleShape)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar prenda",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFF2E92),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Cargando catálogo...",
                        color = Color(0xFF222222),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (clothes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFF2E92), Color(0xFFF5F5F5))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = "Sin prendas",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Catálogo vacío",
                        color = Color(0xFF222222),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Comienza agregando tu primera prenda",
                        color = Color(0xFF666666),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(clothes) { cloth ->
                        ClothCard(
                            cloth = cloth,
                            onEditClick = {
                                selectedCloth = cloth
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                clothViewModel.deleteCloth(cloth.id)
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            ClothDialog(
                title = "NUEVA PRENDA",
                onDismiss = { showAddDialog = false },
                onSave = { clothRequest, imageFile ->
                    clothViewModel.createCloth(clothRequest, imageFile)
                    showAddDialog = false
                }
            )
        }

        selectedCloth?.let { cloth ->
            if (showEditDialog) {
                ClothDialog(
                    title = "EDITAR PRENDA",
                    cloth = cloth,
                    onDismiss = {
                        showEditDialog = false
                        selectedCloth = null
                    },
                    onSave = { clothRequest, imageFile ->
                        clothViewModel.updateCloth(cloth.id, clothRequest, imageFile)
                        showEditDialog = false
                        selectedCloth = null
                    }
                )
            }
        }
    }
}

@Composable
fun ClothCard(
    cloth: Cloth,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (cloth.imageUrl?.isNotEmpty() == true) {
                    AsyncImage(
                        model = "http://10.0.2.2:8000${cloth.imageUrl}",
                        contentDescription = cloth.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                }

                // Quick actions overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                ),
                                startY = 100f
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color(0xFFFF2E92),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = cloth.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                cloth.description?.let { description ->
                    if (description.isNotEmpty()) {
                        Text(
                            text = description,
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            maxLines = 2,
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    cloth.size?.let { size ->
                        if (size.isNotEmpty()) {
                            Text(
                                text = "Talla: $size",
                                fontSize = 12.sp,
                                color = Color(0xFF888888),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    cloth.price?.let { price ->
                        Text(
                            text = "$${"%.2f".format(price)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF2E92)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                cloth.stock?.let { stock ->
                    Text(
                        text = "Stock: $stock unidades",
                        fontSize = 11.sp,
                        color = if (stock > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}