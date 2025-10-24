package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.model.UserModel

interface AuthRepository {

    // ================== LOGIN ==================
    suspend fun login(email: String, password: String): Result<UserModel>

    // ================== REGISTER ==================

    // Registro de admin (crea usuario + empresa)
    suspend fun registerAdmin(
        email: String,
        password: String,
        name: String,
        organizationName: String
    ): Result<UserModel>

    // Registro de worker usando invitation code (solo puede hacerlo un worker)
    suspend fun registerWorker(
        email: String,
        password: String,
        name: String,
        invitationCode: String,
        phoneNumber: String
    ): Result<UserModel>

    // ================== INVITATION CODE ==================
    suspend fun validateInvitationCode(code: String): Result<String>

    // ================== LOGOUT ==================
    suspend fun logout(): Result<Unit>

    // ================== REFRESH TOKEN ==================
    suspend fun refreshAccessToken(): Result<String>

    // ================== CURRENT USER ==================
    suspend fun getCurrentUser(): UserModel?
    fun isUserLoggedIn(): Boolean
}
