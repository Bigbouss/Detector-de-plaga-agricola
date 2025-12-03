package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.model.ScanSessionModel
import com.capstone.cropcare.domain.model.SessionStatus
import kotlinx.coroutines.flow.Flow

interface ScanSessionRepository {

    // ==================== CRUD LOCAL ====================

    suspend fun createSession(session: ScanSessionModel): Result<ScanSessionModel>

    suspend fun getSessionById(sessionId: String): ScanSessionModel?

    fun observeSession(sessionId: String): Flow<ScanSessionModel?>

    suspend fun updateSession(session: ScanSessionModel): Result<Unit>

    suspend fun finishSession(
        sessionId: String,
        notes: String? = null
    ): Result<ScanSessionModel>

    suspend fun cancelSession(sessionId: String): Result<Unit>

    suspend fun incrementHealthyCount(sessionId: String): Result<Unit>
    suspend fun incrementPlagueCount(sessionId: String): Result<Unit>

    fun getAllSessions(): Flow<List<ScanSessionModel>>

    fun getSessionsByStatus(status: SessionStatus): Flow<List<ScanSessionModel>>

    suspend fun getActiveSession(): ScanSessionModel?

    // ==================== SINCRONIZACIÓN ====================

    suspend fun syncSessionsWithBackend(): Result<Unit>

    suspend fun getUnsyncedSessions(): List<ScanSessionModel>
}