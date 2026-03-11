// ChangeUserRoleUseCase.kt
package com.vicpoo.shopy.features.domain.usecase

import com.vicpoo.shopy.features.domain.model.User
import com.vicpoo.shopy.features.domain.repository.UserRepository

class ChangeUserRoleUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(userId: String, newRole: String): User {
        val user = repository.getUserById(userId)
        val updatedUser = user.copy(role = newRole)
        return repository.updateUser(updatedUser)
    }
}