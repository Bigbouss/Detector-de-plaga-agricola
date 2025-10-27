package com.capstone.cropcare.data.repository

import android.util.Log
import com.capstone.cropcare.data.remote.api.InvitationApiService
import com.capstone.cropcare.data.remote.dto.CreateJoinCodeRequest
import com.capstone.cropcare.domain.mappers.toDomain
import com.capstone.cropcare.domain.model.InvitationModel
import com.capstone.cropcare.domain.repository.InvitationRepository
import com.capstone.cropcare.domain.usecase.authUseCase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvitationRepositoryImpl @Inject constructor(
    private val apiService: InvitationApiService,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : InvitationRepository {

    override suspend fun generateInvitation(expiresInDays: Int): Result<InvitationModel> {
        return try {
            // Obtener usuario actual para el nombre de la organización
            val currentUser = getCurrentUserUseCase()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Calcular fecha de expiración (1 día = 24 horas)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.add(Calendar.HOUR, 24 * expiresInDays)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val expiresAtString = dateFormat.format(calendar.time)

            val request = CreateJoinCodeRequest(
                role = "WORKER",
                maxUses = 1,
                expiresAt = expiresAtString
            )

            val response = apiService.createJoinCode(request)

            if (response.isSuccessful && response.body() != null) {
                val joinCode = response.body()!!
                val invitation = joinCode.toDomain(currentUser.organizationName)

                Log.d("InvitationRepository", "✅ Código generado: ${invitation.code}")
                Result.success(invitation)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al generar código"
                Log.e("InvitationRepository", "❌ Error: $errorMsg")
                Result.failure(Exception("No se pudo generar el código"))
            }
        } catch (e: Exception) {
            Log.e("InvitationRepository", "❌ Exception generando código", e)
            Result.failure(Exception("Error de conexión. Verifica tu red."))
        }
    }

    override suspend fun getInvitations(): Flow<List<InvitationModel>> = flow {
        try {
            // Obtener usuario actual para el nombre de la organización
            val currentUser = getCurrentUserUseCase()
            if (currentUser == null) {
                emit(emptyList())
                return@flow
            }

            val response = apiService.getJoinCodes()

            if (response.isSuccessful && response.body() != null) {
                val invitations = response.body()!!.map {
                    it.toDomain(currentUser.organizationName)
                }
                emit(invitations)
                Log.d("InvitationRepository", "✅ ${invitations.size} códigos obtenidos")
            } else {
                Log.e("InvitationRepository", "❌ Error obteniendo códigos")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("InvitationRepository", "❌ Exception obteniendo códigos", e)
            emit(emptyList())
        }
    }

    override suspend fun deleteInvitation(invitationId: String): Result<Unit> {
        // Tu backend no tiene endpoint de DELETE para códigos
        return Result.success(Unit)
    }
}