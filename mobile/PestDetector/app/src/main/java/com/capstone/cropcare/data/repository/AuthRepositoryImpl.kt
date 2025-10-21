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
            val response = apiService.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Guardar tokens
                tokenManager.saveTokens(
                    accessToken = authResponse.tokens.access,
                    refreshToken = authResponse.tokens.refresh
                )

                // Guardar usuario
                val user = authResponse.user.toDomain()
                userPreferences.saveUser(user)

                Log.d("AuthRepository", "✅ Login exitoso: ${user.email}")
                Result.success(user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("AuthRepository", "❌ Login failed: $errorMsg")
                Result.failure(Exception("Login failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Exception en login", e)
            Result.failure(e)
        }
    }

    override suspend fun registerAdmin(
        email: String,
        password: String,
        name: String,
        organizationName: String
    ): Result<UserModel> {
        return try {
            val response = apiService.registerAdmin(
                RegisterAdminRequest(email, password, name, organizationName)
            )

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                tokenManager.saveTokens(
                    authResponse.tokens.access,
                    authResponse.tokens.refresh
                )

                val user = authResponse.user.toDomain()
                userPreferences.saveUser(user)

                Log.d("AuthRepository", "✅ Admin registrado: ${user.email}")
                Result.success(user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error en registro"
                Log.e("AuthRepository", "❌ Register failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Exception en registro admin", e)
            Result.failure(e)
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
            val response = apiService.registerWorker(
                RegisterWorkerRequest(email, password, name, invitationCode)
            )

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                tokenManager.saveTokens(
                    authResponse.tokens.access,
                    authResponse.tokens.refresh
                )

                val user = authResponse.user.toDomain()
                userPreferences.saveUser(user)

                Log.d("AuthRepository", "✅ Worker registrado: ${user.email}")
                Result.success(user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error en registro"
                Log.e("AuthRepository", "❌ Worker register failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Exception en registro worker", e)
            Result.failure(e)
        }
    }


    override suspend fun validateInvitationCode(code: String): Result<String> {
        return try {
            val response = apiService.validateInvitationCode(ValidateCodeRequest(code))

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.valid && body.organization_name != null) {
                    Result.success(body.organization_name)
                } else {
                    Result.failure(Exception(body.error ?: "Código inválido"))
                }
            } else {
                Result.failure(Exception("Error validando código"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Exception validando código", e)
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token != null) {
                apiService.logout("Bearer $token")
            }

            tokenManager.clearTokens()
            userPreferences.clearUser()

            Log.d("AuthRepository", "✅ Logout exitoso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Error en logout", e)
            // Igual limpiamos tokens localmente
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