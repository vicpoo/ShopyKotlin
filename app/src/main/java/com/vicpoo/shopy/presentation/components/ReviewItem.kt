//ReviewItem.kt
package com.vicpoo.shopy.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vicpoo.shopy.domain.model.Review
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReviewItem(
    review: Review,
    isUserReview: Boolean = false,
    showDeleteButton: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(review.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUserReview)
                Color(0xFFFF2E92).copy(alpha = 0.1f)
            else
                Color.White.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = review.userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isUserReview) Color(0xFFFF2E92) else Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    RatingBar(
                        rating = review.rating.toDouble(),
                        starSize = 14.dp
                    )
                }

                if (isUserReview && onEdit != null && onDelete != null) {
                    Row {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color(0xFFFF2E92),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else if (showDeleteButton && onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.comment,
                fontSize = 13.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formattedDate,
                fontSize = 10.sp,
                color = Color.Gray.copy(alpha = 0.6f)
            )
        }
    }
}