//MainScreen.kt
package com.vicpoo.shopy.features.presentation.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.vicpoo.shopy.core.di.Di
import com.vicpoo.shopy.core.utils.Base64ImageUtils
import com.vicpoo.shopy.features.domain.model.Cloth
import com.vicpoo.shopy.features.presentation.components.BecomeSellerDialog
import com.vicpoo.shopy.features.presentation.viewmodels.AuthViewModel
import com.vicpoo.shopy.features.presentation.viewmodels.CartViewModel
import com.vicpoo.shopy.features.presentation.viewmodels.ClothViewModel
import com.vicpoo.shopy.features.presentation.viewmodels.SellerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel,
    navController: NavController,
    onLogout: () -> Unit,
    onNavigateToSeller: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()

    // Crear SellerViewModel
    val sellerViewModel = remember {
        SellerViewModel(
            getCurrentUserUseCase = Di.getCurrentUserUseCase,
            changeUserRoleUseCase = Di.changeUserRoleUseCase,
            getClothesBySellerUseCase = Di.getClothesBySellerUseCase,
            observeClothesBySellerUseCase = Di.observeClothesBySellerUseCase,
            createClothUseCase = Di.createClothUseCase,
            updateClothUseCase = Di.updateClothUseCase,
            deleteClothUseCase = Di.deleteClothUseCase
        )
    }

    // Crear ClothViewModel para obtener productos de la BD
    val clothViewModel = remember {
        ClothViewModel(
            getAllClothesUseCase = Di.getAllClothesUseCase,
            createClothUseCase = Di.createClothUseCase,
            updateClothUseCase = Di.updateClothUseCase,
            deleteClothUseCase = Di.deleteClothUseCase,
            searchClothByNameUseCase = Di.searchClothByNameUseCase,
            searchClothBySizeUseCase = Di.searchClothBySizeUseCase,
            searchClothByPriceRangeUseCase = Di.searchClothByPriceRangeUseCase
        )
    }

    val products by clothViewModel.clothes.collectAsState()
    val isLoadingProducts by clothViewModel.isLoading.collectAsState()

    // Cargar productos al iniciar la pantalla
    LaunchedEffect(Unit) {
        clothViewModel.loadClothes()
    }

    val isSeller by sellerViewModel.isSeller.collectAsState()
    val showDialog by sellerViewModel.showConfirmationDialog.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Manejar vibración (con verificación de permisos)
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun vibrate() {
        // Verificar si tenemos permiso de vibración
        val hasVibratePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.VIBRATE
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasVibratePermission) {
            // Si no hay permiso, simplemente no vibramos
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (e: Exception) {
            // Si hay algún error al vibrar, lo ignoramos
            e.printStackTrace()
        }
    }

    fun addToCart(cloth: Cloth) {
        vibrate()
        cartViewModel.addToCart(cloth.id)
        Toast.makeText(context, "${cloth.name} añadido al carrito", Toast.LENGTH_SHORT).show()
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
        Canvas(modifier = Modifier.fillMaxSize()) {
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

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.background(Color(0xFF0B0B0F))
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        "Menú",
                        modifier = Modifier.padding(20.dp),
                        color = Color.White,
                        fontSize = 22.sp
                    )

                    // Opción de Carrito
                    NavigationDrawerItem(
                        label = { Text("Carrito", color = Color.White) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToCart()
                        },
                        icon = {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    )

                    // Opción de Vendedor
                    NavigationDrawerItem(
                        label = {
                            Text(
                                if (isSeller) "Panel de Vendedor" else "Ser Vendedor",
                                color = if (isSeller) Color(0xFFFF2E92) else Color.White
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (isSeller) {
                                onNavigateToSeller()
                            } else {
                                sellerViewModel.showBecomeSellerDialog()
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Store,
                                contentDescription = null,
                                tint = if (isSeller) Color(0xFFFF2E92) else Color.White
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.padding(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xEEBD2B6E)
                        )
                    ) {
                        Text("Cerrar sesión")
                    }
                }
            }
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                currentUser?.name ?: "Usuario",
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = onNavigateToCart) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Carrito",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            ) { padding ->
                if (isLoadingProducts) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF2E92))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(padding)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(products) { product ->
                            ProductCard(
                                cloth = product,
                                onAddToCart = { addToCart(product) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para hacerse vendedor
    if (showDialog) {
        BecomeSellerDialog(
            onConfirm = { sellerViewModel.becomeSeller() },
            onDismiss = { sellerViewModel.hideBecomeSellerDialog() },
            isLoading = sellerViewModel.isLoading.collectAsState().value
        )
    }
}

@Composable
fun ProductCard(
    cloth: Cloth,
    onAddToCart: () -> Unit
) {
    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }

    // Cargar imagen Base64 si existe
    LaunchedEffect(cloth.image) {
        if (cloth.image != null && cloth.image!!.isNotEmpty() && imageBitmap == null) {
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoadingImage -> CircularProgressIndicator(
                        color = Color(0xFFFF2E92),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(30.dp)
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
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    cloth.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "$${cloth.price ?: 0}",
                    color = Color(0xFFFF2E92),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Tallas: ${cloth.size ?: "N/A"}",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            // Botón de agregar al carrito
            IconButton(
                onClick = onAddToCart,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color(0xFFFF2E92).copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    Icons.Default.AddShoppingCart,
                    contentDescription = "Agregar al carrito",
                    tint = Color(0xFFFF2E92)
                )
            }
        }
    }
}