//AddReviewDialog.kt
package com.vicpoo.shopy.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vicpoo.shopy.presentation.components.RatingBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewDialog(
    initialRating: Int = 0,
    initialComment: String = "",
    isEditing: Boolean = false,
    onSubmit: (rating: Int, comment: String) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    var rating by remember { mutableIntStateOf(initialRating) }
    var comment by remember { mutableStateOf(initialComment) }
    var ratingError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1C1C1E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Editar reseña" else "Calificar producto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Calificación con estrellas
                Text(
                    text = "Tu calificación",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                RatingBar(
                    rating = rating.toDouble(),
                    onRatingChanged = { newRating ->
                        rating = newRating.toInt()
                        ratingError = null
                    },
                    starSize = 40.dp,
                    interactive = true
                )

                if (ratingError != null) {
                    Text(
                        text = ratingError!!,
                        fontSize = 12.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Comentario
                Text(
                    text = "Tu opinión",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("¿Qué te pareció el producto?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF2E92),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFFF2E92)
                    ),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (rating == 0) {
                                ratingError = "Selecciona una calificación"
                            } else {
                                onSubmit(rating, comment)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF2E92)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(if (isEditing) "Actualizar" else "Publicar")
                        }
                    }
                }
            }
        }
    }
}