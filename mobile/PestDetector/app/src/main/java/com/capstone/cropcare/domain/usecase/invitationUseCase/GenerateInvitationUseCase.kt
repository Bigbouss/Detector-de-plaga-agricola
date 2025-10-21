package com.capstone.cropcare.domain.usecase.invitationUseCase

import com.capstone.cropcare.domain.model.InvitationModel
import com.capstone.cropcare.domain.repository.InvitationRepository
import javax.inject.Inject

class GenerateInvitationUseCase @Inject constructor(
    private val invitationRepository: InvitationRepository
) {
    suspend operator fun invoke(expiresInDays: Int = 7): Result<InvitationModel> {
        if (expiresInDays < 1 || expiresInDays > 30) {
            return Result.failure(Exception("Los días deben estar entre 1 y 30"))
        }

        return invitationRepository.generateInvitation(expiresInDays)
    }
}