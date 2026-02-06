//Di.kt
package com.vicpoo.shopy.core.di

import com.vicpoo.shopy.core.network.RetrofitProvider
import com.vicpoo.shopy.features.data.remote.ClothApi
import com.vicpoo.shopy.features.data.remote.UserApi
import com.vicpoo.shopy.features.data.repository.ClothRepositoryImpl
import com.vicpoo.shopy.features.data.repository.UserRepositoryImpl
import com.vicpoo.shopy.features.domain.repository.ClothRepository
import com.vicpoo.shopy.features.domain.repository.UserRepository
import com.vicpoo.shopy.features.domain.usecase.*

object Di {
    private val userApi: UserApi by lazy {
        RetrofitProvider.retrofit.create(UserApi::class.java)
    }

    private val clothApi: ClothApi by lazy {
        RetrofitProvider.retrofit.create(ClothApi::class.java)
    }

    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(userApi)
    }

    private val clothRepository: ClothRepository by lazy {
        ClothRepositoryImpl(clothApi)
    }

    val registerUseCase: RegisterUseCase by lazy {
        RegisterUseCase(userRepository)
    }

    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(userRepository)
    }

    val getAllClothesUseCase: GetAllClothesUseCase by lazy {
        GetAllClothesUseCase(clothRepository)
    }

    val createClothUseCase: CreateClothUseCase by lazy {
        CreateClothUseCase(clothRepository)
    }

    val updateClothUseCase: UpdateClothUseCase by lazy {
        UpdateClothUseCase(clothRepository)
    }

    val deleteClothUseCase: DeleteClothUseCase by lazy {
        DeleteClothUseCase(clothRepository)
    }

    val searchClothByNameUseCase: SearchClothByNameUseCase by lazy {
        SearchClothByNameUseCase(clothRepository)
    }

    val searchClothBySizeUseCase: SearchClothBySizeUseCase by lazy {
        SearchClothBySizeUseCase(clothRepository)
    }

    val searchClothByPriceRangeUseCase: SearchClothByPriceRangeUseCase by lazy {
        SearchClothByPriceRangeUseCase(clothRepository)
    }
}