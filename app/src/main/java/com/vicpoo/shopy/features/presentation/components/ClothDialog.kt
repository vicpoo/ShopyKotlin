package com.vicpoo.shopy.features.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vicpoo.shopy.features.domain.model.Cloth
import com.vicpoo.shopy.features.domain.model.ClothRequest
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothDialog(
    title: String,
    cloth: Cloth? = null,
    onDismiss: () -> Unit,
    onSave: (ClothRequest, File?) -> Unit
) {
    var name by remember { mutableStateOf(cloth?.name ?: "") }
    var description by remember { mutableStateOf(cloth?.description ?: "") }
    var size by remember { mutableStateOf(cloth?.size ?: "") }
    var price by remember { mutableStateOf(cloth?.price?.toString() ?: "") }
    var stock by remember { mutableStateOf(cloth?.stock?.toString() ?: "") }
    var imageFile by remember { mutableStateOf<File?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color(0xFFFF2E92)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre de la prenda *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.ShoppingBag, contentDescription = "Nombre")
                        }
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = size,
                            onValueChange = { size = it },
                            label = { Text("Talla") },
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(Icons.Default.Straighten, contentDescription = "Talla")
                            }
                        )

                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Precio") },
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(Icons.Default.AttachMoney, contentDescription = "Precio")
                            }
                        )
                    }

                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Inventory, contentDescription = "Stock")
                        }
                    )

                    // Image upload section
                    Text(
                        text = "Imagen de la prenda",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )

                    // TODO: Implement image picker

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF666666)
                            )
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                val clothRequest = ClothRequest(
                                    name = name,
                                    description = if (description.isNotEmpty()) description else null,
                                    size = if (size.isNotEmpty()) size else null,
                                    price = price.toDoubleOrNull(),
                                    stock = stock.toIntOrNull(),
                                    imageUrl = cloth?.imageUrl
                                )
                                onSave(clothRequest, imageFile)
                            },
                            enabled = name.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF2E92)
                            )
                        ) {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}