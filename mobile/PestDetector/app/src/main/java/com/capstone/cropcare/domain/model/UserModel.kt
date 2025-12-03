package com.capstone.cropcare.domain.model

data class UserModel(
    val id: Int,
    val email: String,
    val role: UserRole,
    val empresaId: Int
)

enum class UserRole {
    ADMIN,
    WORKER
}