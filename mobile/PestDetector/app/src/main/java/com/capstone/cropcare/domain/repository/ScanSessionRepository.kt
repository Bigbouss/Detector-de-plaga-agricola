//package com.capstone.cropcare.domain.repository
//
//import com.capstone.cropcare.domain.model.ScanSessionModel
//import com.capstone.cropcare.domain.model.SessionStatus
//import kotlinx.coroutines.flow.Flow
//
//interface ScanSessionRepository {
//
//    // ==================== CRUD LOCAL ====================
//
//    /**
//     * Crea una nueva sesión de escaneo
//     */
//    suspend fun createSession(session: ScanSessionModel): Result<ScanSessionModel>
//
//    /**
//     * Obtiene una sesión por ID
//     */
//    suspend fun getSessionById(sessionId: String): ScanSessionModel?
//
//    /**
//     * Observa una sesión en tiempo real
//     */
//    fun observeSession(sessionId: String): Flow<ScanSessionModel?>
//
//    /**
//     * Actualiza una sesión existente
//     */
//    suspend fun updateSession(session: ScanSessionModel): Result<Unit>
//
//    /**
//     * Finaliza una sesión (cambia estado a COMPLETED)
//     */
//    suspend fun finishSession(
//        sessionId: String,
//        notes: String? = null
//    ): Result<ScanSessionModel>
//
//    /**
//     * Cancela una sesión (cambia estado a CANCELLED)
//     */
//    suspend fun cancelSession(sessionId: String): Result<Unit>
//
//    /**
//     * Incrementa contadores de una sesión
//     */
//    suspend fun incrementHealthyCount(sessionId: String): Result<Unit>
//    suspend fun incrementPlagueCount(sessionId: String): Result<Unit>
//
//    /**
//     * Obtiene todas las sesiones del worker actual
//     */
//    fun getAllSessions(): Flow<List<ScanSessionModel>>
//
//    /**
//     * Obtiene sesiones por estado
//     */
//    fun getSessionsByStatus(status: SessionStatus): Flow<List<ScanSessionModel>>
//
//    /**
//     * Obtiene la sesión activa actual (si existe)
//     */
//    suspend fun getActiveSession(): ScanSessionModel?
//
//    // ==================== SINCRONIZACIÓN ====================
//
//    /**
//     * Sincroniza sesiones con el backend
//     */
//    suspend fun syncSessionsWithBackend(): Result<Unit>
//
//    /**
//     * Obtiene sesiones no sincronizadas
//     */
//    suspend fun getUnsyncedSessions(): List<ScanSessionModel>
//}