package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.model.InvitationModel
import kotlinx.coroutines.flow.Flow

interface InvitationRepository {
    suspend fun generateInvitation(expiresInDays: Int = 7): Result<InvitationModel>
    suspend fun getInvitations(): Flow<List<InvitationModel>>
    suspend fun deleteInvitation(invitationId: String): Result<Unit>
}