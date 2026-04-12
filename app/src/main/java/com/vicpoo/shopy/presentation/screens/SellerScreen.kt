//SellerScreen.kt
package com.vicpoo.shopy.presentation.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
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
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // 🔥 HEADER PRO
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }

                    Text(
                        text = "Mis Productos",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Text(
                    text = "${clothes.size} productos publicados",
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 56.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 📊 STATS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Total", clothes.size.toString(), Color(0xFFFF2E92))
                StatItem("Stock", clothes.count { (it.stock ?: 0) > 0 }.toString(), Color.Green)
                StatItem("Agotados", clothes.count { (it.stock ?: 0) == 0 }.toString(), Color.Red)
            }

            Spacer(modifier = Modifier.height(16.dp))

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

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "No tienes productos aún",
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF2E92)
                            )
                        ) {
                            Text("Agregar producto")
                        }
                    }
                }
            } else {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = clothes,
                        key = { it.id }
                    ) { cloth ->
                        SellerProductItem(
                            cloth = cloth,
                            onEdit = { selectedCloth = cloth },
                            onDelete = { viewModel.deleteCloth(cloth.id) }
                        )
                    }
                }
            }
        }

        // ➕ FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFFFF2E92),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }

        // ⏳ LOADING
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

    // 🧾 DIALOG
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
fun StatItem(title: String, value: String, color: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold)
            Text(title, color = Color.Gray, fontSize = 12.sp)
        }
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
        if (!cloth.image.isNullOrEmpty()) {
            isLoadingImage = true
            try {
                val bitmap = Base64ImageUtils.base64ToBitmap(cloth.image!!)
                imageBitmap = bitmap?.asImageBitmap()
            } catch (_: Exception) {
            } finally {
                isLoadingImage = false
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🖼️ IMAGEN
            Box(
                modifier = Modifier
                    .size(75.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoadingImage -> CircularProgressIndicator(
                        color = Color(0xFFFF2E92),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp)
                    )

                    imageBitmap != null -> Image(
                        bitmap = imageBitmap!!,
                        contentDescription = cloth.name,
                        modifier = Modifier.fillMaxSize()
                    )

                    else -> Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = cloth.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "$${cloth.price ?: 0}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF2E92)
                )

                Spacer(modifier = Modifier.height(4.dp))

                val stock = cloth.stock ?: 0
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (stock > 0)
                                Color.Green.copy(alpha = 0.2f)
                            else
                                Color.Red.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (stock > 0) "Stock: $stock" else "Agotado",
                        fontSize = 11.sp,
                        color = if (stock > 0) Color.Green else Color.Red
                    )
                }

                if (!cloth.size.isNullOrBlank()) {
                    Text(
                        text = "Tallas: ${cloth.size}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, null, tint = Color(0xFFFF2E92))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                }
            }
        }
    }
}