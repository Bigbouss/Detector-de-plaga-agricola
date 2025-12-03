package com.capstone.cropcare.data.repository

import android.util.Log
import com.capstone.cropcare.data.local.dao.ScanSessionDao
import com.capstone.cropcare.data.remote.api.ScannerApiService
import com.capstone.cropcare.domain.mappers.*
import com.capstone.cropcare.domain.model.ScanSessionModel
import com.capstone.cropcare.domain.model.SessionStatus
import com.capstone.cropcare.domain.repository.ScanResultRepository
import com.capstone.cropcare.domain.repository.ScanSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanSessionRepositoryImpl @Inject constructor(
    private val scanSessionDao: ScanSessionDao,
    private val scannerApiService: ScannerApiService,
    private val scanResultRepository: ScanResultRepository,
    private val userSessionManager: com.capstone.cropcare.domain.manager.UserSessionManager
) : ScanSessionRepository {

    // ==================== CRUD LOCAL ====================

    override suspend fun createSession(session: ScanSessionModel): Result<ScanSessionModel> {
        return try {
            // Guardar localmente
            scanSessionDao.insertSession(session.toEntity())
            Log.d("ScanSessionRepo", "Sesión creada localmente: ${session.id}")

            // Intentar sincronizar con backend
            try {
                val empresaId = userSessionManager.getEmpresaId()
                val dto = session.toCreateDTO(empresaId)
                val response = scannerApiService.createSession(dto)

                if (response.isSuccessful) {
                    // Marcar como sincronizada
                    val syncedSession = session.copy(syncedWithBackend = true)
                    scanSessionDao.updateSession(syncedSession.toEntity())
                    Log.d("ScanSessionRepo", "Sesión sincronizada con backend: ${session.id}")
                } else {
                    Log.w("ScanSessionRepo", "No se pudo sincronizar sesión: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.w("ScanSessionRepo", "Error sincronizando sesión con backend", e)

            }

            Result.success(session)
        } catch (e: Exception) {
            Log.e("ScanSessionRepo", "Error creando sesión", e)
            Result.failure(e)
        }
    }

    override suspend fun getSessionById(sessionId: String): ScanSessionModel? {
        return scanSessionDao.getSessionById(sessionId)?.toDomain()
    }

    override fun observeSession(sessionId: String): Flow<ScanSessionModel?> {
        return scanSessionDao.observeSession(sessionId).map { it?.toDomain() }
    }

    override suspend fun updateSession(session: ScanSessionModel): Result<Unit> {
        return try {
            scanSessionDao.updateSession(session.toEntity())

            // Si está sincronizada, actualizar en backend
            if (session.syncedWithBackend) {
                try {
                    val dto = session.toUpdateDTO()
                    scannerApiService.updateSession(session.id, dto)
                } catch (e: Exception) {
                    Log.w("ScanSessionRepo", "Error actualizando sesión en backend", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ScanSessionRepo", "Error actualizando sesión", e)
            Result.failure(e)
        }
    }

    override suspend fun finishSession(
        sessionId: String,
        notes: String?
    ): Result<ScanSessionModel> {
        return try {
            val session = getSessionById(sessionId)
                ?: return Result.failure(Exception("Sesión no encontrada"))

            val updatedSession = session.copy(
                status = SessionStatus.COMPLETED,
                finishedAt = System.currentTimeMillis(),
                notes = notes
            )

            scanSessionDao.updateSession(updatedSession.toEntity())
            Log.d("ScanSessionRepo", "Sesión finalizada: $sessionId")

            // Intentar finalizar en backend
            if (session.syncedWithBackend) {
                try {
                    val notesMap = notes?.let { mapOf("notes" to it) } ?: emptyMap()
                    scannerApiService.finishSession(sessionId, notesMap)
                } catch (e: Exception) {
                    Log.w("ScanSessionRepo", "Error finalizando sesión en backend", e)
                }
            }

            Result.success(updatedSession)
        } catch (e: Exception) {
            Log.e("ScanSessionRepo", "Error finalizando sesión", e)
            Result.failure(e)
        }
    }

    override suspend fun cancelSession(sessionId: String): Result<Unit> {
        return try {
            val session = getSessionById(sessionId)
                ?: return Result.failure(Exception("Sesión no encontrada"))

            val cancelledSession = session.copy(
                status = SessionStatus.CANCELLED,
                finishedAt = System.currentTimeMillis()
            )

            scanSessionDao.updateSession(cancelledSession.toEntity())
            Log.d("ScanSessionRepo", "Sesión cancelada: $sessionId")

            // Intentar cancelar en backend
            if (session.syncedWithBackend) {
                try {
                    scannerApiService.cancelSession(sessionId)
                } catch (e: Exception) {
                    Log.w("ScanSessionRepo", "Error cancelando sesión en backend", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ScanSessionRepo", "Error cancelando sesión", e)
            Result.failure(e)
        }
    }

    override suspend fun incrementHealthyCount(sessionId: String): Result<Unit> {
        return try {
            scanSessionDao.incrementHealthyCount(sessionId)
            scanSessionDao.incrementTotalScans(sessionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ScanSessionRepo", "Error incrementando contador", e)
            Result.failure(e)
        }
    }

    override suspend fun incrementPlagueCount(sessionId: String): Result<Unit> {
        return try {
            scanSessionDao.incrementPlagueCount(sessionId)
            scanSessionDao.incrementTotalScans(sessionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ScanSessionRepo", "Error incrementando contador", e)
            Result.failure(e)
        }
    }

    override fun getAllSessions(): Flow<List<ScanSessionModel>> {
        return scanSessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSessionsByStatus(status: SessionStatus): Flow<List<ScanSessionModel>> {
        return scanSessionDao.getSessionsByStatus(status.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getActiveSession(): ScanSessionModel? {
        return scanSessionDao.getActiveSession()?.toDomain()
    }

    // ==================== SINCRONIZACIÓN ====================

    override suspend fun syncSessionsWithBackend(): Result<Unit> {
        return try {
            val unsyncedSessions = getUnsyncedSessions()

            if (unsyncedSessions.isEmpty()) {
                Log.d("ScanSessionRepo", "ℹ️ No hay sesiones por sincronizar")
                return Result.success(Unit)
            }

            Log.d("ScanSessionRepo", "🔄 Sincronizando ${unsyncedSessions.size} sesiones...")

            // Obtener empresa_id del usuario actual
            val empresaId = userSessionManager.getEmpresaId()

            // Agrupar resultados de escaneo por sesión
            val scanResultsMap = mutableMapOf<String, List<com.capstone.cropcare.domain.model.ScanResultModel>>()
            for (session in unsyncedSessions) {
                val results = scanResultRepository.getScanResultsBySession(session.id).first()
                scanResultsMap[session.id] = results.filter { !it.syncedWithBackend }
            }

            // Crear request de sincronización
            val syncRequest = unsyncedSessions.toSyncRequest(scanResultsMap, empresaId)

            // Enviar al backend
            val response = scannerApiService.syncSessions(syncRequest)

            if (response.isSuccessful) {
                val syncResponse = response.body()!!

                Log.d("ScanSessionRepo", """
                    Sincronización completada:
                    - Sesiones sincronizadas: ${syncResponse.totalSynced}
                    - Errores: ${syncResponse.totalErrors}
                """.trimIndent())

                // Marcar sesiones sincronizadas como tal
                syncResponse.synced.forEach { syncedItem ->
                    syncedItem.sessionId?.let { sessionId ->
                        val session = getSessionById(sessionId)
                        session?.let {
                            val synced = it.copy(syncedWithBackend = true)
                            scanSessionDao.updateSession(synced.toEntity())
                        }
                    }
                }

                Result.success(Unit)
            } else {
                Log.e("ScanSessionRepo", "Error en sincronización: ${response.code()}")
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }

        } catch (e: Exception) {
            Log.e("ScanSessionRepo", "Error sincronizando sesiones", e)
            Result.failure(e)
        }
    }

    override suspend fun getUnsyncedSessions(): List<ScanSessionModel> {
        return scanSessionDao.getUnsyncedSessions().map { it.toDomain() }
    }

    suspend fun fetchSessionsFromBackend(): Result<List<ScanSessionModel>> {
        return try {
            val response = scannerApiService.getMySessions()

            if (response.isSuccessful) {
                val sessions = response.body()?.map { it.toDomain() } ?: emptyList()

                // Guardar en base de datos local
                sessions.forEach { session ->
                    scanSessionDao.insertSession(session.toEntity())
                }

                Log.d("ScanSessionRepo", "${sessions.size} sesiones descargadas del backend")
                Result.success(sessions)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ScanSessionRepo", "Error descargando sesiones", e)
            Result.failure(e)
        }
    }
}