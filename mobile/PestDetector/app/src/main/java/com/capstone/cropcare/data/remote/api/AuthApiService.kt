package com.capstone.cropcare.data.remote.api

import com.capstone.cropcare.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("auth/login/")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("auth/register_admin/")
    suspend fun registerAdmin(
        @Body request: RegisterAdminRequest
    ): Response<AuthResponse>

    @POST("auth/register_worker/")
    suspend fun registerWorker(
        @Body request: RegisterWorkerRequest
    ): Response<AuthResponse>

    @POST("invitations/validate_code/")
    suspend fun validateInvitationCode(
        @Body request: ValidateCodeRequest
    ): Response<ValidateCodeResponse>

    @POST("auth/refresh/")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    @POST("auth/logout/")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Unit>
}