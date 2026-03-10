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
    onLogout: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val products = listOf(
        Product("Nike Air Max", "$120", "38, 39, 40, 41", R.drawable.nike),
        Product("Adidas Ultraboost", "$140", "39, 40, 41, 42", R.drawable.adidas),
        Product("Puma Runner", "$95", "37, 38, 39, 40", R.drawable.puma)
    )
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(

        drawerState = drawerState,

        drawerContent = {

            ModalDrawerSheet {

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Menú",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(16.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Vendedor") },
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.Store, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    ) {

        Scaffold(

            topBar = {

                TopAppBar(

                    title = {
                        Text(text = currentUser?.name ?: "Usuario")
                    },

                    navigationIcon = {

                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {

                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "menu"
                            )
                        }
                    },

                    actions = {

                        IconButton(onClick = { }) {

                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "notificaciones"
                            )
                        }
                    }
                )
            }

        ) { paddingValues ->

            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {

                items(products) { product ->

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {

                        Row(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Image(
                                painter = painterResource(id = product.image),
                                contentDescription = product.name,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(15.dp))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {

                                Text(
                                    text = product.name,
                                    fontSize = 18.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Precio: ${product.price}"
                                )

                                Text(
                                    text = "Tallas: ${product.sizes}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}