//UserDto.kt
package com.vicpoo.shopy.features.data.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id_usuario") val id: Int? = null,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("lastname") val lastname: String? = null
)

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("lastname") val lastname: String? = null
)

data class AuthResponseDto(
    @SerializedName("id_usuario") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String?,
    @SerializedName("lastname") val lastname: String?,
    @SerializedName("token") val token: String? = null
)