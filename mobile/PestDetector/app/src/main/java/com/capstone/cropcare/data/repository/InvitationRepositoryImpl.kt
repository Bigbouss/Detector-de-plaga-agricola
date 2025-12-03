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
            val currentUser = getCurrentUserUseCase()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Calcular fecha de expiración en UTC
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.add(Calendar.DAY_OF_YEAR, expiresInDays)

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

                // Convertir a dominio usando el empresaId del usuario
                val invitation = joinCode.toDomain(
                    empresaId = currentUser.empresaId
                )

                Log.d("InvitationRepo", "✅ Código generado: ${invitation.code}")
                Result.success(invitation)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "No autorizado"
                    403 -> "No tienes permisos para generar códigos"
                    else -> "No se pudo generar el código"
                }
                Log.e("InvitationRepo", "❌ Error ${response.code()}: $errorMsg")
                Result.failure(Exception(errorMsg))
            }

        } catch (e: Exception) {
            Log.e("InvitationRepo", "❌ Error al generar invitación", e)
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    override suspend fun getInvitations(): Flow<List<InvitationModel>> = flow {
        try {
            val currentUser = getCurrentUserUseCase()
            if (currentUser == null) {
                Log.w("InvitationRepo", "⚠️ Usuario no autenticado")
                emit(emptyList())
                return@flow
            }

            val response = apiService.getJoinCodes()

            if (response.isSuccessful && response.body() != null) {
                val invitations = response.body()!!.map { joinCodeDto ->
                    joinCodeDto.toDomain(
                        empresaId = currentUser.empresaId
                    )
                }

                Log.d("InvitationRepo", "✅ ${invitations.size} códigos obtenidos")
                emit(invitations)
            } else {
                Log.e("InvitationRepo", "❌ Error al obtener códigos: ${response.code()}")
                emit(emptyList())
            }

        } catch (e: Exception) {
            Log.e("InvitationRepo", "❌ Error al obtener invitaciones", e)
            emit(emptyList())
        }
    }

    override suspend fun deleteInvitation(invitationId: String): Result<Unit> {
        // El backend actual no soporta DELETE de códigos
        // Podrías implementar un endpoint de "revoke" si lo agregas al backend
        Log.w("InvitationRepo", "⚠️ Eliminación de códigos no implementada en el backend")
        return Result.success(Unit)
    }
}