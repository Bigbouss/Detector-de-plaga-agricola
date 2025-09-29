package com.capstone.cropcare.data.repository

import com.capstone.cropcare.data.response.UserResponse
import com.capstone.cropcare.data.response.toDomain
import com.capstone.cropcare.domain.entity.UserEntity
import com.capstone.cropcare.domain.repository.AuthRepository

class AuthRepositoryImpl: AuthRepository {
    override fun doLogin(user: String, password: String): UserEntity {
        val userResponse: UserResponse = UserResponse(
            "", "", "", "",1)

        return userResponse.toDomain()
    }
}