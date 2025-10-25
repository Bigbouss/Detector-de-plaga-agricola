package com.capstone.cropcare.data.repository

import android.util.Log
import com.capstone.cropcare.data.local.preferences.TokenManager
import com.capstone.cropcare.data.local.preferences.UserPreferences
import com.capstone.cropcare.domain.mappers.toDomain
import com.capstone.cropcare.data.remote.api.AuthApiService
import com.capstone.cropcare.data.remote.dto.*
import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager,
    private val userPreferences: UserPreferences
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserModel> {
        return try {
            // 1. Obtener tokens con Djoser
            val loginResponse = apiService.login(LoginRequest(email, password))

            if (!loginResponse.isSuccessful || loginResponse.body() == null) {
                val errorMsg = loginResponse.errorBody()?.string() ?: "Error desconocido"
                Log.e("AuthRepository", "❌ Login failed: $errorMsg")
                return Result.failure(Exception("Credenciales inválidas"))
            }

            val tokens = loginResponse.body()!!

            // 2. Guardar tokens
            tokenManager.saveTokens(
                accessToken = tokens.access,
                refreshToken = tokens.refresh
            )

            // 3. Obtener datos del usuario usando /me endpoint
            val meResponse = apiService.getMe("Bearer ${tokens.access}")

            if (!meResponse.isSuccessful || meResponse.body() == null) {
                Log.e("AuthRepository", "❌ Error obteniendo datos del usuario")
                return Result.failure(Exception("Error obteniendo información del usuario"))
            }

            val user = meResponse.body()!!.toDomain()
            userPreferences.saveUser(user)

            Log.d("AuthRepository", "✅ Login exitoso: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Exception en login", e)
            Result.failure(Exception("Error de conexión. Verifica tu red."))
        }
    }

    override suspend fun registerAdmin(
        email: String,
        password: String,
        name: String,
        organizationName: String
    ): Result<UserModel> {
        return try {
            val nameParts = name.split(" ", limit = 2)
            val firstName = nameParts.getOrNull(0) ?: ""
            val lastName = nameParts.getOrNull(1) ?: ""

            val response = apiService.registerAdmin(
                RegisterAdminRequest(email, password, firstName, lastName, organizationName)
            )

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                tokenManager.saveTokens(
                    authResponse.tokens.access,
                    authResponse.tokens.refresh
                )

                val user = authResponse.toDomain()
                userPreferences.saveUser(user)

                Log.d("AuthRepository", "✅ Admin registrado: ${user.email}")
                Result.success(user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error en registro"
                Log.e("AuthRepository", "❌ Register failed: $errorMsg")
                Result.failure(Exception("Error al registrar. Verifica los datos."))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Exception en registro admin", e)
            Result.failure(Exception("Error de conexión. Verifica tu red."))
        }
    }

    override suspend fun registerWorker(
        email: String,
        password: String,
        name: String,
        invitationCode: String,
        phoneNumber: String
    ): Result<UserModel> {
        return try {
            val nameParts = name.split(" ", limit = 2)
            val firstName = nameParts.getOrNull(0) ?: ""
            val lastName = nameParts.getOrNull(1) ?: ""

            val response = apiService.registerWorker(
                RegisterWorkerRequest(email, password, firstName, lastName, invitationCode)
            )

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                tokenManager.saveTokens(
                    authResponse.tokens.access,
                    authResponse.tokens.refresh
                )

                val user = authResponse.toDomain()
                userPreferences.saveUser(user)

                Log.d("AuthRepository", "✅ Worker registrado: ${user.email}")
                Result.success(user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error en registro"
                Log.e("AuthRepository", "❌ Worker register failed: $errorMsg")
                Result.failure(Exception("Código inválido o error al registrar."))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Exception en registro worker", e)
            Result.failure(Exception("Error de conexión. Verifica tu red."))
        }
    }

    override suspend fun validateInvitationCode(code: String): Result<String> {
        return try {
            val response = apiService.validateInvitationCode(ValidateCodeRequest(code))

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.valid && body.companyName != null) {
                    Result.success(body.companyName)
                } else {
                    Result.failure(Exception(body.error ?: "Código inválido"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error validando código"
                Log.e("AuthRepository", "❌ Validate code failed: $errorMsg")
                Result.failure(Exception("Código inválido o expirado"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Exception validando código", e)
            Result.failure(Exception("Error de conexión. Verifica tu red."))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            tokenManager.clearTokens()
            userPreferences.clearUser()
            Log.d("AuthRepository", "✅ Logout exitoso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Error en logout", e)
            tokenManager.clearTokens()
            userPreferences.clearUser()
            Result.success(Unit)
        }
    }

    override suspend fun refreshAccessToken(): Result<String> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
                ?: return Result.failure(Exception("No refresh token"))

            val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))

            if (response.isSuccessful && response.body() != null) {
                val newAccessToken = response.body()!!.access
                tokenManager.saveTokens(newAccessToken, refreshToken)
                Result.success(newAccessToken)
            } else {
                Result.failure(Exception("Failed to refresh token"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Error refreshing token", e)
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): UserModel? {
        return userPreferences.userFlow.first()
    }

    override fun isUserLoggedIn(): Boolean {
        return tokenManager.hasValidTokens()
    }
}