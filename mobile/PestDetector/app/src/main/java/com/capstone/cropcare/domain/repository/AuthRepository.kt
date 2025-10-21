package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.model.UserModel

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserModel>

    suspend fun registerAdmin(
        email: String,
        password: String,
        name: String,
        organizationName: String
    ): Result<UserModel>

    suspend fun registerWorker(
        email: String,
        password: String,
        name: String,
        invitationCode: String,
        phoneNumber: String
    ): Result<UserModel>

    suspend fun validateInvitationCode(code: String): Result<String> // Retorna org name

    suspend fun logout(): Result<Unit>

    suspend fun refreshAccessToken(): Result<String>

    suspend fun getCurrentUser(): UserModel?

    fun isUserLoggedIn(): Boolean
}