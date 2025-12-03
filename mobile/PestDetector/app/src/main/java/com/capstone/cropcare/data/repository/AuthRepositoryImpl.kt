package com.capstone.cropcare.data.repository

import android.util.Log
import com.capstone.cropcare.data.local.preferences.TokenManager
import com.capstone.cropcare.data.local.preferences.UserPreferences
import com.capstone.cropcare.data.remote.api.AuthApiService
import com.capstone.cropcare.data.remote.decodeJwtPayload
import com.capstone.cropcare.data.remote.dto.*
import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.model.UserRole
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
            val response = apiService.login(LoginRequest(email = email, password = password))

            if (!response.isSuccessful || response.body() == null) {
                return Result.failure(Exception("Credenciales inválidas"))
            }

            val tokens = response.body()!!

            // Guardar tokens
            tokenManager.saveTokens(tokens.access, tokens.refresh)

            // Decodificar JWT para obtener datos del usuario
            val payload = decodeJwtPayload(tokens.access)

            val user = UserModel(
                id = payload.userId ?: -1,
                email = payload.email ?: email,
                role = when (payload.role?.uppercase()) {
                    "ADMIN" -> UserRole.ADMIN
                    "WORKER" -> UserRole.WORKER
                    else -> UserRole.WORKER
                },
                empresaId = payload.empresaId ?: -1
            )

            // Guardar usuario en preferencias
            userPreferences.saveUser(user)

            Log.d("AuthRepository", "✅ Login exitoso: ${user.email} - Role: ${user.role}")
            Result.success(user)

        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Error en login", e)
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    override suspend fun registerAdmin(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        companyName: String,
        phone: String?,
        taxId: String
    ): Result<UserModel> {
        return try {
            val request = RegisterAdminRequest(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                phone = phone ?: "",
                companyName = companyName,
                taxId = taxId
            )

            Log.d("AuthRepository", "Enviando registro admin: $request")

            val response = apiService.registerAdmin(request)

            if (!response.isSuccessful || response.body() == null) {
                val errorMsg = when (response.code()) {
                    400 -> "Datos inválidos. Verifica el formato del RUT y los campos."
                    409 -> "El email ya está registrado"
                    else -> "Error al registrar administrador (código ${response.code()})"
                }
                Log.e("AuthRepository", "Error ${response.code()}: ${response.errorBody()?.string()}")
                return Result.failure(Exception(errorMsg))
            }

            val registerResponse = response.body()!!
            Log.d("AuthRepository", "Admin registrado: userId=${registerResponse.userId}, empresaId=${registerResponse.empresaId}")

            // Backend no devuelve tokens en el registro → hacer login automático
            login(email, password)

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en registerAdmin", e)
            Result.failure(Exception("Error de conexión: ${e.message}"))
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
            val parts = name.trim().split(" ", limit = 2)
            val firstName = parts.getOrNull(0) ?: ""
            val lastName = parts.getOrNull(1) ?: ""

            val request = RegisterWorkerRequest(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                phone = phoneNumber,
                joinCode = invitationCode.uppercase().trim()
            )

            val response = apiService.registerWorker(request)

            if (!response.isSuccessful || response.body() == null) {
                val errorMsg = when (response.code()) {
                    400 -> "Código inválido o expirado"
                    409 -> "El email ya está registrado"
                    else -> "Error al registrar trabajador"
                }
                return Result.failure(Exception(errorMsg))
            }

            val registerResponse = response.body()!!
            Log.d("AuthRepository", "Worker registrado: userId=${registerResponse.userId}, empresaId=${registerResponse.empresaId}")

            // Backend no devuelve tokens en el registro → hacer login automático
            login(email, password)

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en registerWorker", e)
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    override suspend fun validateInvitationCode(code: String): Result<String> {
        return try {
            val response = apiService.validateJoinCode(
                ValidateCodeRequest(code.uppercase().trim())
            )

            if (!response.isSuccessful || response.body() == null) {
                val errorMsg = when (response.code()) {
                    404 -> "Código no encontrado"
                    400 -> "Código expirado o revocado"
                    else -> "Error al validar código"
                }
                return Result.failure(Exception(errorMsg))
            }

            val validationResponse = response.body()!!

            if (validationResponse.valid && validationResponse.empresa != null) {
                Log.d("AuthRepository", "✅ Código válido: ${validationResponse.empresa}")
                Result.success(validationResponse.empresa)
            } else {
                val error = validationResponse.error ?: "Código inválido o expirado"
                Log.w("AuthRepository", "Código inválido: $error")
                Result.failure(Exception(error))
            }

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al validar código", e)
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            tokenManager.clearTokens()
            userPreferences.clearUser()
            Log.d("AuthRepository", "Logout exitoso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en logout", e)
            Result.failure(Exception("Error al cerrar sesión"))
        }
    }

    override suspend fun refreshAccessToken(): Result<String> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
                ?: return Result.failure(Exception("No hay refresh token"))

            val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))

            if (response.isSuccessful && response.body() != null) {
                val newAccessToken = response.body()!!.access
                tokenManager.saveTokens(newAccessToken, refreshToken)
                Log.d("AuthRepository", "Token refrescado exitosamente")
                Result.success(newAccessToken)
            } else {
                Log.e("AuthRepository", "Error al refrescar token: ${response.code()}")
                Result.failure(Exception("Sesión expirada"))
            }

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en refreshAccessToken", e)
            Result.failure(Exception("Error al refrescar token"))
        }
    }

    override suspend fun getCurrentUser(): UserModel? {
        return try {
            userPreferences.userFlow.first()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al obtener usuario actual", e)
            null
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return tokenManager.hasValidTokens()
    }
}