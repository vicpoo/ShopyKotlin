//CaptchatLocalDataSource.kt
package com.vicpoo.shopy.data.local.captcha

import com.vicpoo.shopy.R
import com.vicpoo.shopy.data.local.entity.CaptchaImageEntity

class CaptchaLocalDataSource {

    fun getImages(): List<CaptchaImageEntity> {
        return listOf(
            // Perros (correctos)
            CaptchaImageEntity(1, R.drawable.perrito, true),
            CaptchaImageEntity(2, R.drawable.perrito2, true),
            CaptchaImageEntity(3, R.drawable.perrito3, true),
            CaptchaImageEntity(4, R.drawable.perrito4, true),
            CaptchaImageEntity(5, R.drawable.perrito5, true),

            // Gatos (incorrectos)
            CaptchaImageEntity(6, R.drawable.gato, false),
            CaptchaImageEntity(7, R.drawable.gato2, false),
            CaptchaImageEntity(8, R.drawable.gato3, false),
            CaptchaImageEntity(9, R.drawable.gato4, false),
            CaptchaImageEntity(10, R.drawable.gato5, false),
        )
    }
}