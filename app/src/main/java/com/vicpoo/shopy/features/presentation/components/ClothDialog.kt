// ClothDialog.kt
package com.vicpoo.shopy.features.presentation.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vicpoo.shopy.core.utils.Base64ImageUtils
import com.vicpoo.shopy.core.utils.rememberImagePickerHandler
import com.vicpoo.shopy.features.domain.model.Cloth
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothDialog(
    title: String,
    cloth: Cloth? = null,
    onDismiss: () -> Unit,
    onSave: (Cloth, File?) -> Unit
) {
    val context = LocalContext.current
    val imagePicker = rememberImagePickerHandler()

    var name by remember { mutableStateOf(cloth?.name ?: "") }
    var description by remember { mutableStateOf(cloth?.description ?: "") }
    var size by remember { mutableStateOf(cloth?.size ?: "") }
    var price by remember { mutableStateOf(cloth?.price?.toString() ?: "") }
    var stock by remember { mutableStateOf(cloth?.stock?.toString() ?: "") }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var imagePreview by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isLoadingPreview by remember { mutableStateOf(false) }

    // Obtener los valores del estado
    val currentImageFile by imagePicker.imageFile
    val showOptions by imagePicker.showImageOptions
    val permissionDenied by imagePicker.permissionDenied

    // Cargar preview de la imagen seleccionada (desde archivo)
    LaunchedEffect(currentImageFile) {
        selectedImageFile = currentImageFile
        currentImageFile?.let { file ->
            isLoadingPreview = true
            try {
                val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                imagePreview = bitmap?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingPreview = false
            }
        }
    }

    // Cargar preview de imagen existente (Base64)
    LaunchedEffect(cloth?.image) {
        if (cloth?.image != null && cloth.image!!.isNotEmpty() && selectedImageFile == null) {
            isLoadingPreview = true
            try {
                // Verificar si es Base64
                if (cloth.image!!.startsWith("/9j/") || cloth.image!!.length > 100) {
                    val bitmap = Base64ImageUtils.base64ToBitmap(cloth.image!!)
                    imagePreview = bitmap?.asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingPreview = false
            }
        }
    }

    // Limpiar cuando se cierra el diálogo
    DisposableEffect(Unit) {
        onDispose {
            imagePicker.clearImage()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp)
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header con gradiente rosa
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFF2E92),
                                    Color(0xFFFF6AA6)
                                )
                            )
                        ),
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
                    // Campo Nombre (obligatorio)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre de la prenda *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.ShoppingBag, contentDescription = "Nombre")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF2E92),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color(0xFFFF2E92)
                        )
                    )

                    // Campo Descripción
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF2E92),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color(0xFFFF2E92)
                        )
                    )

                    // Fila de Talla y Precio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = size,
                            onValueChange = { size = it },
                            label = { Text("Talla (separadas por coma)") },
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(Icons.Default.Straighten, contentDescription = "Talla")
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2E92),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color(0xFFFF2E92)
                            )
                        )

                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Precio") },
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(Icons.Default.AttachMoney, contentDescription = "Precio")
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2E92),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color(0xFFFF2E92)
                            )
                        )
                    }

                    // Campo Stock
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Inventory, contentDescription = "Stock")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF2E92),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color(0xFFFF2E92)
                        )
                    )

                    // Sección de imagen mejorada
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Imagen de la prenda",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF666666)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Preview de imagen
                            if (isLoadingPreview) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Gray.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFFF2E92)
                                    )
                                }
                            } else if (imagePreview != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Gray.copy(alpha = 0.2f))
                                ) {
                                    Image(
                                        bitmap = imagePreview!!,
                                        contentDescription = "Preview",
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Botón para cambiar imagen
                                    IconButton(
                                        onClick = { imagePicker.showOptions() },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.5f),
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Cambiar",
                                            tint = Color.White
                                        )
                                    }
                                }
                            } else {
                                // Botón para seleccionar imagen
                                Button(
                                    onClick = { imagePicker.showOptions() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF2E92).copy(alpha = 0.1f)
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Color(0xFFFF2E92),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Seleccionar imagen",
                                        color = Color(0xFFFF2E92)
                                    )
                                }
                            }

                            // Mostrar información del archivo seleccionado
                            if (selectedImageFile != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.Green,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Archivo: ${selectedImageFile?.name}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            } else if (cloth?.image != null && cloth.image!!.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFFFF2E92),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val sizeKB = Base64ImageUtils.getBase64SizeInKB(cloth.image!!)
                                    Text(
                                        text = "Imagen existente (${sizeKB}KB)",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón Cancelar
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF666666)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(Color.Gray, Color.Gray)
                                )
                            )
                        ) {
                            Text("Cancelar")
                        }

                        // Botón Guardar
                        Button(
                            onClick = {
                                val clothToSave = Cloth(
                                    id = cloth?.id ?: "",
                                    name = name,
                                    description = if (description.isNotEmpty()) description else null,
                                    size = if (size.isNotEmpty()) size else null,
                                    price = price.toDoubleOrNull(),
                                    stock = stock.toIntOrNull(),
                                    image = cloth?.image, // La imagen se procesará en el ViewModel/Repository
                                    sellerId = cloth?.sellerId ?: ""
                                )
                                onSave(clothToSave, selectedImageFile)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = name.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF2E92),
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        ) {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }

    // Diálogo de opciones de imagen
    if (showOptions) {
        ImageOptionsDialog(
            onDismiss = { imagePicker.hideOptions() },
            onCameraClick = imagePicker.requestCamera,
            onGalleryClick = imagePicker.requestGallery
        )
    }

    // Diálogo de permiso denegado
    if (permissionDenied) {
        PermissionDeniedDialog(
            onDismiss = { imagePicker.hideOptions() },
            onSettingsClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
                imagePicker.hideOptions()
            }
        )
    }
}