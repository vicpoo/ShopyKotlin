package com.vicpoo.shopy.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.vicpoo.shopy.presentation.viewmodels.CaptchaViewModel
import com.vicpoo.shopy.presentation.components.CaptchaGrid

@Composable
fun CaptchaScreen(
    viewModel: CaptchaViewModel = hiltViewModel()
) {
    val images by viewModel.images

    Column {

        CaptchaGrid(
            images = images,
            selected = viewModel.selected,
            onClick = { viewModel.toggleSelection(it) }
        )

        Button(
            onClick = {
                val result = viewModel.validate()
                println("Captcha válido: $result")
            }
        ) {
            Text("Validar")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCaptcha()
    }
}