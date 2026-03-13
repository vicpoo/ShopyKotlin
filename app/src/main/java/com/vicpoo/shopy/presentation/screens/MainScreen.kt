//MainScreen.kt
package com.vicpoo.shopy.presentation.screens

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vicpoo.shopy.core.utils.Base64ImageUtils
import com.vicpoo.shopy.domain.model.Cloth
import com.vicpoo.shopy.presentation.components.BecomeSellerDialog
import com.vicpoo.shopy.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    onLogout: () -> Unit,
    onNavigateToSeller: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados del ViewModel
    val currentUser by viewModel.currentUser.collectAsState()
    val products by viewModel.products.collectAsState()
    val cartItemCount by viewModel.cartItemCount.collectAsState()
    val unreadNotifications by viewModel.unreadNotifications.collectAsState()
    val isSeller by viewModel.isSeller.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val error by viewModel.error.collectAsState()
    val showDialog by viewModel.showBecomeSellerDialog.collectAsState()

    // Estado para el drawer
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // Estado para scroll y refresh manual
    val listState = rememberLazyListState()

    // Vibrator setup
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    // Mostrar errores
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Función para vibrar al agregar al carrito
    fun vibrate() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Layout principal
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
        // Decoración con Canvas
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

        // Modal Navigation Drawer
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.background(Color(0xFF0B0B0F))
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    // Header del drawer con información del usuario
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            "Hola, ${currentUser?.name ?: "Usuario"}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            currentUser?.email ?: "",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Divider(color = Color.Gray.copy(alpha = 0.3f))

                    // Items del drawer
                    NavigationDrawerItem(
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Notificaciones", color = Color.White)
                                if (unreadNotifications > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Badge(
                                        containerColor = Color(0xFFFF2E92),
                                        content = { Text(unreadNotifications.toString()) }
                                    )
                                }
                            }
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToNotifications()
                        },
                        icon = {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    )

                    NavigationDrawerItem(
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Carrito", color = Color.White)
                                if (cartItemCount > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Badge(
                                        containerColor = Color(0xFFFF2E92),
                                        content = { Text(cartItemCount.toString()) }
                                    )
                                }
                            }
                        },
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
                                viewModel.showBecomeSellerDialog()
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

                    Spacer(modifier = Modifier.weight(1f))

                    // Botón de logout al final
                    Button(
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                viewModel.logout()
                                onLogout()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEEBD2B6E)
                        )
                    ) {
                        Text("Cerrar sesión")
                    }
                }
            }
        ) {
            // Scaffold principal
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    "SHOPY",
                                    color = Color(0xFFFF2E92),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!isOnline) {
                                    Text(
                                        "📶 Modo offline",
                                        color = Color.Yellow,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menú",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            // Botón de notificaciones con badge
                            IconButton(onClick = onNavigateToNotifications) {
                                Box {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Notificaciones",
                                        tint = Color.White
                                    )
                                    if (unreadNotifications > 0) {
                                        Badge(
                                            containerColor = Color(0xFFFF2E92),
                                            content = { Text(unreadNotifications.toString()) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 8.dp, y = (-8).dp)
                                        )
                                    }
                                }
                            }

                            // Botón de carrito con badge
                            IconButton(onClick = onNavigateToCart) {
                                Box {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = "Carrito",
                                        tint = Color.White
                                    )
                                    if (cartItemCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFFFF2E92),
                                            content = { Text(cartItemCount.toString()) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 8.dp, y = (-8).dp)
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Contenido principal
                    if (isLoading && products.isEmpty()) {
                        // Loading inicial
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFF2E92)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Cargando productos...",
                                    color = Color.Gray
                                )
                            }
                        }
                    } else if (products.isEmpty()) {
                        // Empty state
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
                                    "No hay productos disponibles",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.refreshProducts() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF2E92)
                                    ),
                                    enabled = isOnline
                                ) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    } else {
                        // Lista de productos con swipe to refresh manual
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = products,
                                key = { it.id }
                            ) { product ->
                                ProductCard(
                                    cloth = product,
                                    onAddToCart = {
                                        vibrate()
                                        viewModel.addToCart(product.id, product.name)
                                        Toast.makeText(
                                            context,
                                            "${product.name} añadido al carrito",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }

                    // Indicador de refresh manual
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = Color(0xFFFF2E92),
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(36.dp)
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialog para convertirse en vendedor
    if (showDialog) {
        BecomeSellerDialog(
            onConfirm = { viewModel.becomeSeller() },
            onDismiss = { viewModel.hideBecomeSellerDialog() },
            isLoading = isLoading
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

    // Cargar imagen cuando cambie el producto
    LaunchedEffect(cloth.id, cloth.image) {
        if (cloth.image != null && cloth.image!!.isNotEmpty()) {
            isLoadingImage = true
            try {
                // Detectar si es Base64
                if (cloth.image!!.startsWith("/9j/") || cloth.image!!.length > 100) {
                    val bitmap = Base64ImageUtils.base64ToBitmap(cloth.image!!)
                    imageBitmap = bitmap?.asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingImage = false
            }
        } else {
            imageBitmap = null
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.1f)
        ),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoadingImage -> {
                        CircularProgressIndicator(
                            color = Color(0xFFFF2E92),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    imageBitmap != null -> {
                        Image(
                            bitmap = imageBitmap!!,
                            contentDescription = cloth.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                    else -> {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cloth.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )

                Text(
                    text = "$${String.format("%.2f", cloth.price ?: 0.0)}",
                    color = Color(0xFFFF2E92),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                if (!cloth.size.isNullOrBlank()) {
                    Text(
                        text = "Tallas: ${cloth.size}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                if (cloth.stock != null && cloth.stock > 0) {
                    Text(
                        text = "Stock: ${cloth.stock}",
                        color = Color.Green.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
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