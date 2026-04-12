//RatingBar.kt
package com.vicpoo.shopy.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RatingBar(
    rating: Double,
    onRatingChanged: ((Double) -> Unit)? = null,
    starSize: androidx.compose.ui.unit.Dp = 24.dp,
    interactive: Boolean = false
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            val starIcon = when {
                rating >= i -> Icons.Filled.Star
                rating >= i - 0.5 -> Icons.Filled.StarHalf
                else -> Icons.Filled.StarBorder
            }

            if (interactive && onRatingChanged != null) {
                IconButton(
                    onClick = { onRatingChanged(i.toDouble()) },
                    modifier = Modifier.size(starSize)
                ) {
                    Icon(
                        starIcon,
                        contentDescription = null,
                        tint = Color(0xFFFFB800)
                    )
                }
            } else {
                Icon(
                    starIcon,
                    contentDescription = null,
                    tint = Color(0xFFFFB800),
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}