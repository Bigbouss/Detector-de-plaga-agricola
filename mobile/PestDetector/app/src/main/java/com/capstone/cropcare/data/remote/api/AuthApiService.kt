package com.capstone.cropcare.data.remote.api

import com.capstone.cropcare.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    /**
     * Login con JWT
     * POST /api/accounts/auth/login/
     */
    @POST("api/accounts/auth/login/")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>

    /**
     * Refresh del access token
     * POST /api/accounts/auth/token/refresh/
     */
    @POST("api/accounts/auth/token/refresh/")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    /**
     * Registro de ADMIN (crea empresa y usuario)
     * POST /api/accounts/auth/register-admin/
     */
    @POST("api/accounts/auth/register-admin/")
    suspend fun registerAdmin(
        @Body request: RegisterAdminRequest
    ): Response<RegisterAdminResponse>

    /**
     * Registro de WORKER (con join code)
     * POST /api/accounts/auth/register-worker/
     */
    @POST("api/accounts/auth/register-worker/")
    suspend fun registerWorker(
        @Body request: RegisterWorkerRequest
    ): Response<RegisterWorkerResponse>

    /**
     * Validar código de invitación
     * POST /api/joincodes/validate-code/
     */
    @POST("api/joincodes/validate-code/")
    suspend fun validateJoinCode(
        @Body request: ValidateCodeRequest
    ): Response<ValidateCodeResponse>
}