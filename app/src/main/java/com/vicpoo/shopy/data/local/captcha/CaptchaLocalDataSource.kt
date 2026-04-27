package com.vicpoo.shopy.data.local.captcha

import com.vicpoo.shopy.R
import com.vicpoo.shopy.data.local.entity.CaptchaImageEntity
class CaptchaLocalDataSource {

    fun getImages(): List<CaptchaImageEntity> {
        return listOf(
            CaptchaImageEntity(1, R.drawable.perrito, true),
            CaptchaImageEntity(2, R.drawable.perrito2, true),
            CaptchaImageEntity(3, R.drawable.perrito3, false),
            CaptchaImageEntity(4, R.drawable.perrito4, false),
            CaptchaImageEntity(1, R.drawable.perrito5, true),
            CaptchaImageEntity(2, R.drawable.gato, true),
            CaptchaImageEntity(3, R.drawable.gato2, false),
            CaptchaImageEntity(4, R.drawable.gato3, false),
            CaptchaImageEntity(3, R.drawable.gato4, false),
            CaptchaImageEntity(4, R.drawable.gato5, false),

            // hasta 10
        )
    }
}