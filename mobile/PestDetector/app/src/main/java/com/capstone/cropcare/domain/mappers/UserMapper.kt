package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.remote.dto.UserDto
import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.model.UserRole

fun UserDto.toDomain(): UserModel {
    return UserModel(
        uid = id,
        email = email,
        name = name,
        role = when (role) {
            "ADMIN" -> UserRole.ADMIN
            "WORKER" -> UserRole.WORKER
            else -> UserRole.WORKER
        },
        organizationId = organization_id,
        organizationName = organization_name,
        mustChangePassword = must_change_password
    )
}