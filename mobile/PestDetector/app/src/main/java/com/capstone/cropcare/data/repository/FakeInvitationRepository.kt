package com.capstone.cropcare.data.repository

import com.capstone.cropcare.domain.model.InvitationModel
import com.capstone.cropcare.domain.repository.InvitationRepository
import com.capstone.cropcare.domain.usecase.authUseCase.GetCurrentUserUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeInvitationRepository @Inject constructor(
    private val fakeAuthRepository: FakeAuthRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : InvitationRepository {

    override suspend fun generateInvitation(expiresInDays: Int): Result<InvitationModel> {
        delay(500)

        val currentUser = getCurrentUserUseCase()
            ?: return Result.failure(Exception("Usuario no autenticado"))

        // 👇 OPCIONAL: Limitar cantidad de códigos activos
        val activeCodesCount = fakeAuthRepository.getActiveInvitationsCount(currentUser.organizationId)
        if (activeCodesCount >= 10) { // Máximo 10 códigos activos
            return Result.failure(Exception("Has alcanzado el límite de 10 códigos activos. Espera a que expiren o sean usados."))
        }

        val invitationInfo = fakeAuthRepository.generateInvitationCode(
            adminId = currentUser.uid,
            organizationId = currentUser.organizationId,
            organizationName = currentUser.organizationName,
            expiresInDays = expiresInDays
        )

        val invitation = InvitationModel(
            id = "inv_${invitationInfo.code}",
            code = invitationInfo.code,
            organizationId = currentUser.organizationId,
            organizationName = invitationInfo.organizationName,
            createdBy = currentUser.uid,
            isActive = !invitationInfo.isUsed,
            isUsed = invitationInfo.isUsed,
            createdAt = invitationInfo.createdAt, // 👈 Fecha individual
            expiresAt = invitationInfo.expiresAt
        )

        return Result.success(invitation)
    }

    override suspend fun getInvitations(): Flow<List<InvitationModel>> = flow {
        val currentUser = getCurrentUserUseCase()
        if (currentUser != null) {
            delay(500)

            val invitations = fakeAuthRepository
                .getAllInvitationsForOrganization(currentUser.organizationId)
                .map { info ->
                    val now = System.currentTimeMillis()
                    val isExpired = now > info.expiresAt

                    InvitationModel(
                        id = "inv_${info.code}",
                        code = info.code,
                        organizationId = currentUser.organizationId,
                        organizationName = info.organizationName,
                        createdBy = currentUser.uid,
                        isUsed = info.isUsed,
                        isActive = !info.isUsed && !isExpired,
                        createdAt = info.createdAt, // 👈 Individual
                        expiresAt = info.expiresAt
                    )
                }

            emit(invitations)
        } else {
            emit(emptyList())
        }
    }

    override suspend fun deleteInvitation(invitationId: String): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }
}