//UserApi.kt
package com.vicpoo.shopy.features.data.remote

import com.vicpoo.shopy.features.data.dto.AuthResponseDto
import com.vicpoo.shopy.features.data.dto.LoginRequestDto
import com.vicpoo.shopy.features.data.dto.RegisterRequestDto
import com.vicpoo.shopy.features.data.dto.UserDto
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @GET("api/users")
    suspend fun getAllUsers(): List<UserDto>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Int): UserDto

    @POST("api/users")
    suspend fun createUser(@Body user: UserDto): UserDto

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: UserDto): UserDto

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Void>
}