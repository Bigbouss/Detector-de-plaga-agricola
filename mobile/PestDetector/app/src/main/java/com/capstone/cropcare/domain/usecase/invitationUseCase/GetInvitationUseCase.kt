package com.capstone.cropcare.domain.usecase.invitationUseCase

import com.capstone.cropcare.domain.model.InvitationModel
import com.capstone.cropcare.domain.repository.InvitationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInvitationsUseCase @Inject constructor(
    private val invitationRepository: InvitationRepository
) {
    suspend operator fun invoke(): Flow<List<InvitationModel>> {
        return invitationRepository.getInvitations()
    }
}