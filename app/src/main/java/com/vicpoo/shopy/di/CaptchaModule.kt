//CaptchaModule.kt
package com.vicpoo.shopy.di

import com.vicpoo.shopy.data.local.captcha.CaptchaLocalDataSource
import com.vicpoo.shopy.data.repository.CaptchaRepositoryImpl
import com.vicpoo.shopy.domain.repository.CaptchaRepository
import com.vicpoo.shopy.domain.usecase.GenerateCaptchaUseCase
import com.vicpoo.shopy.domain.usecase.ValidateCaptchaUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object CaptchaModule {

    @Provides
    fun provideLocalDataSource() = CaptchaLocalDataSource()

    @Provides
    fun provideRepository(
        local: CaptchaLocalDataSource
    ): CaptchaRepository {
        return CaptchaRepositoryImpl(local)
    }

    @Provides
    fun provideGenerateCaptchaUseCase(
        repo: CaptchaRepository
    ) = GenerateCaptchaUseCase(repo)

    @Provides
    fun provideValidateCaptchaUseCase() =
        ValidateCaptchaUseCase()
}