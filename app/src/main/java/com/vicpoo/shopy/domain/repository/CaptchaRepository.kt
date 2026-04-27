package com.vicpoo.shopy.domain.repository

import com.vicpoo.shopy.domain.model.CaptchaImage

interface CaptchaRepository {
    fun getImages(): List<CaptchaImage>
}