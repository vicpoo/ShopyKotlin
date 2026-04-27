//CaptchaImageEntity.kt
package com.vicpoo.shopy.data.local.entity

data class CaptchaImageEntity(
    val id: Int,
    val resId: Int,
    val isCorrect: Boolean
)