package com.capstone.cropcare.data.remote.api

import com.capstone.cropcare.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    // Login
    @POST("orgs/auth/token/")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>

    // Register Admin
    @POST("orgs/auth/register-admin/")
    suspend fun registerAdmin(
        @Body request: RegisterAdminRequest
    ): Response<AuthResponse>

    // Register Worker
    @POST("orgs/auth/register-worker/")
    suspend fun registerWorker(
        @Body request: RegisterWorkerRequest
    ): Response<AuthResponse>

    // Validar código de invitación
    @POST("orgs/invitations/validate/")
    suspend fun validateInvitationCode(
        @Body request: ValidateCodeRequest
    ): Response<ValidateCodeResponse>

    // Obtener datos del usuario autenticado
    @GET("orgs/auth/me/")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): Response<MeResponse>

    // Refresh token
    @POST("orgs/auth/token/refresh/")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    // Logout
    @POST("auth/logout/")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Unit>
}
