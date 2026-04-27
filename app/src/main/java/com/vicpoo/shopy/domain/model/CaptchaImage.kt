//CaptchaImage.kt
package com.vicpoo.shopy.domain.model

data class CaptchaImage(
    val id: Int,
    val resId: Int,
    val isCorrect: Boolean
)