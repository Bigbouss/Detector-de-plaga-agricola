package com.capstone.cropcare.data.local.dao

import androidx.room.*
import com.capstone.cropcare.data.local.entity.ScanSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ScanSessionEntity)

    @Update
    suspend fun updateSession(session: ScanSessionEntity)

    @Query("SELECT * FROM scan_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): ScanSessionEntity?

    @Query("SELECT * FROM scan_sessions WHERE sessionId = :sessionId")
    fun observeSession(sessionId: String): Flow<ScanSessionEntity?>

    @Query("SELECT * FROM scan_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<ScanSessionEntity>>

    @Query("SELECT * FROM scan_sessions WHERE status = :status ORDER BY startedAt DESC")
    fun getSessionsByStatus(status: String): Flow<List<ScanSessionEntity>>

    @Query("SELECT * FROM scan_sessions WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSession(): ScanSessionEntity?

    @Query("SELECT * FROM scan_sessions WHERE syncedWithBackend = 0")
    suspend fun getUnsyncedSessions(): List<ScanSessionEntity>

    @Query("UPDATE scan_sessions SET totalScans = totalScans + 1 WHERE sessionId = :sessionId")
    suspend fun incrementTotalScans(sessionId: String)

    @Query("UPDATE scan_sessions SET healthyCount = healthyCount + 1 WHERE sessionId = :sessionId")
    suspend fun incrementHealthyCount(sessionId: String)

    @Query("UPDATE scan_sessions SET plagueCount = plagueCount + 1 WHERE sessionId = :sessionId")
    suspend fun incrementPlagueCount(sessionId: String)

    @Delete
    suspend fun deleteSession(session: ScanSessionEntity)

    @Query("DELETE FROM scan_sessions")
    suspend fun deleteAll()
}