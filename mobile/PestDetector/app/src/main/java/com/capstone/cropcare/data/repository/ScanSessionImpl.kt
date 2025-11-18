//package com.capstone.cropcare.data.repository
//
//import android.util.Log
//import com.capstone.cropcare.data.local.dao.ScanSessionDao
//import com.capstone.cropcare.domain.mappers.toEntity
//import com.capstone.cropcare.domain.mappers.toDomain
//import com.capstone.cropcare.domain.model.ScanSessionModel
//import com.capstone.cropcare.domain.model.SessionStatus
//import com.capstone.cropcare.domain.repository.ScanSessionRepository
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.map
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class ScanSessionRepositoryImpl @Inject constructor(
//    private val scanSessionDao: ScanSessionDao
//    // TODO: Agregar API service cuando tengas el endpoint en el backend
//) : ScanSessionRepository {
//
//    override suspend fun createSession(session: ScanSessionModel): Result<ScanSessionModel> {
//        return try {
//            scanSessionDao.insertSession(session.toEntity())
//            Log.d("ScanSessionRepo", "✅ Sesión creada: ${session.id}")
//            Result.success(session)
//        } catch (e: Exception) {
//            Log.e("ScanSessionRepo", "❌ Error creando sesión", e)
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun getSessionById(sessionId: String): ScanSessionModel? {
//        return scanSessionDao.getSessionById(sessionId)?.toDomain()
//    }
//
//    override fun observeSession(sessionId: String): Flow<ScanSessionModel?> {
//        return scanSessionDao.observeSession(sessionId).map { it?.toDomain() }
//    }
//
//    override suspend fun updateSession(session: ScanSessionModel): Result<Unit> {
//        return try {
//            scanSessionDao.updateSession(session.toEntity())
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Log.e("ScanSessionRepo", "❌ Error actualizando sesión", e)
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun finishSession(
//        sessionId: String,
//        notes: String?
//    ): Result<ScanSessionModel> {
//        return try {
//            val session = getSessionById(sessionId)
//                ?: return Result.failure(Exception("Sesión no encontrada"))
//
//            val updatedSession = session.copy(
//                status = SessionStatus.COMPLETED,
//                finishedAt = System.currentTimeMillis(),
//                notes = notes
//            )
//
//            scanSessionDao.updateSession(updatedSession.toEntity())
//            Log.d("ScanSessionRepo", "✅ Sesión finalizada: $sessionId")
//            Result.success(updatedSession)
//        } catch (e: Exception) {
//            Log.e("ScanSessionRepo", "❌ Error finalizando sesión", e)
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun cancelSession(sessionId: String): Result<Unit> {
//        return try {
//            val session = getSessionById(sessionId)
//                ?: return Result.failure(Exception("Sesión no encontrada"))
//
//            val cancelledSession = session.copy(
//                status = SessionStatus.CANCELLED,
//                finishedAt = System.currentTimeMillis()
//            )
//
//            scanSessionDao.updateSession(cancelledSession.toEntity())
//            Log.d("ScanSessionRepo", "⚠️ Sesión cancelada: $sessionId")
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Log.e("ScanSessionRepo", "❌ Error cancelando sesión", e)
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun incrementHealthyCount(sessionId: String): Result<Unit> {
//        return try {
//            scanSessionDao.incrementHealthyCount(sessionId)
//            scanSessionDao.incrementTotalScans(sessionId)
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Log.e("ScanSessionRepo", "❌ Error incrementando contador", e)
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun incrementPlagueCount(sessionId: String): Result<Unit> {
//        return try {
//            scanSessionDao.incrementPlagueCount(sessionId)
//            scanSessionDao.incrementTotalScans(sessionId)
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Log.e("ScanSessionRepo", "❌ Error incrementando contador", e)
//            Result.failure(e)
//        }
//    }
//
//    override fun getAllSessions(): Flow<List<ScanSessionModel>> {
//        return scanSessionDao.getAllSessions().map { entities ->
//            entities.map { it.toDomain() }
//        }
//    }
//
//    override fun getSessionsByStatus(status: SessionStatus): Flow<List<ScanSessionModel>> {
//        return scanSessionDao.getSessionsByStatus(status.name).map { entities ->
//            entities.map { it.toDomain() }
//        }
//    }
//
//    override suspend fun getActiveSession(): ScanSessionModel? {
//        return scanSessionDao.getActiveSession()?.toDomain()
//    }
//
//    override suspend fun syncSessionsWithBackend(): Result<Unit> {
//        // TODO: Implementar cuando tengas el endpoint en el backend
//        return Result.success(Unit)
//    }
//
//    override suspend fun getUnsyncedSessions(): List<ScanSessionModel> {
//        return scanSessionDao.getUnsyncedSessions().map { it.toDomain() }
//    }
//}