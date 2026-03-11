//MainScreen.kt
package com.vicpoo.shopy.features.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.vicpoo.shopy.features.presentation.viewmodels.AuthViewModel
import com.vicpoo.shopy.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import com.vicpoo.shopy.core.di.Di
import com.vicpoo.shopy.features.presentation.components.BecomeSellerDialog
import com.vicpoo.shopy.features.presentation.viewmodels.SellerViewModel

data class Product(
    val name: String,
    val price: String,
    val sizes: String,
    val image: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    onLogout: () -> Unit,
    onNavigateToSeller: () -> Unit
) {
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

    val isSeller by sellerViewModel.isSeller.collectAsState()
    val showDialog by sellerViewModel.showConfirmationDialog.collectAsState()

    val products = listOf(
        Product("Nike Air Max", "$120", "38, 39, 40, 41", R.drawable.nike),
        Product("Adidas Ultraboost", "$140", "39, 40, 41, 42", R.drawable.adidas),
        Product("Puma Runner", "$95", "37, 38, 39, 40", R.drawable.puma)
    )
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                            IconButton(onClick = { }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
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
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(products) { product ->
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Image(
                                    painter = painterResource(product.image),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        product.name,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        "Precio: ${product.price}",
                                        color = Color(0xFFFF2E92)
                                    )

                                    Text(
                                        "Tallas: ${product.sizes}",
                                        color = Color.LightGray
                                    )
                                }
                            }
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