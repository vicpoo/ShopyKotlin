//CaptchaRepositoryImpl.kt
package com.vicpoo.shopy.data.repository

import com.vicpoo.shopy.data.local.captcha.CaptchaLocalDataSource
import com.vicpoo.shopy.domain.model.CaptchaImage
import com.vicpoo.shopy.domain.repository.CaptchaRepository

class CaptchaRepositoryImpl(
    private val local: CaptchaLocalDataSource
) : CaptchaRepository {

    override fun getImages(): List<CaptchaImage> {
        return local.getImages().map {
            CaptchaImage(
                id = it.id,
                resId = it.resId,
                isCorrect = it.isCorrect
            )
        }
    }
}