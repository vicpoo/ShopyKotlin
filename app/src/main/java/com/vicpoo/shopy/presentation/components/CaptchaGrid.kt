//CaptchaGrid
package com.vicpoo.shopy.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.vicpoo.shopy.domain.model.CaptchaImage

@Composable
fun CaptchaGrid(
    images: List<CaptchaImage>,
    selected: List<Int>,
    onClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2)
    ) {
        items(images) { image ->
            Image(
                painter = painterResource(id = image.resId),
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onClick(image.id) }
                    .border(
                        width = 2.dp,
                        color = if (selected.contains(image.id))
                            Color.Green
                        else
                            Color.Transparent
                    )
            )
        }
    }
}