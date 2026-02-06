//UserMapper.kt
package com.vicpoo.shopy.features.data.mapper

import com.vicpoo.shopy.features.data.dto.*
import com.vicpoo.shopy.features.domain.model.*

fun UserDto.toDomain(): User {
    return User(
        id = id ?: 0,
        email = email,
        password = password ?: "",
        name = name,
        lastname = lastname
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = if (id > 0) id else null,
        email = email,
        password = if (password.isNotEmpty()) password else null,
        name = name,
        lastname = lastname
    )
}

fun RegisterRequest.toDto(): RegisterRequestDto {
    return RegisterRequestDto(
        email = email,
        password = password,
        name = name,
        lastname = lastname
    )
}

fun LoginRequest.toDto(): LoginRequestDto {
    return LoginRequestDto(
        email = email,
        password = password
    )
}

fun AuthResponseDto.toDomain(): AuthResponse {
    return AuthResponse(
        id_usuario = id,
        email = email,
        name = name,
        lastname = lastname,
        token = token
    )
}