package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.remote.dto.AuthResponse
import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.model.UserRole

/**
 * Convierte AuthResponse (del registro) a UserModel del dominio
 */
fun AuthResponse.toDomain(): UserModel {
    return UserModel(
        uid = user.id.toString(),
        email = user.email,
        name = buildString {
            if (user.firstName.isNotBlank()) append(user.firstName)
            if (user.lastName.isNotBlank()) {
                if (isNotEmpty()) append(" ")
                append(user.lastName)
            }
            if (isEmpty()) append(user.username)
        },
        role = when (profile.role.uppercase()) {
            "ADMIN" -> UserRole.ADMIN
            "WORKER" -> UserRole.WORKER
            else -> UserRole.WORKER
        },
        organizationId = empresa.id.toString(),
        organizationName = empresa.name,
        mustChangePassword = false
    )
}

/**
 * Convierte MeResponse (del login) a UserModel del dominio
 */
fun com.capstone.cropcare.data.remote.dto.MeResponse.toDomain(): UserModel {
    return UserModel(
        uid = user.id.toString(),
        email = user.email,
        name = buildString {
            if (user.firstName.isNotBlank()) append(user.firstName)
            if (user.lastName.isNotBlank()) {
                if (isNotEmpty()) append(" ")
                append(user.lastName)
            }
            if (isEmpty()) append(user.username)
        },
        role = when (profile.role.uppercase()) {
            "ADMIN" -> UserRole.ADMIN
            "WORKER" -> UserRole.WORKER
            else -> UserRole.WORKER
        },
        organizationId = empresa?.id?.toString() ?: "",
        organizationName = empresa?.name ?: "",
        mustChangePassword = false
    )
}