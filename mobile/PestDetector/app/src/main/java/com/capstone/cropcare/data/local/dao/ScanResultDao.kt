package com.capstone.cropcare.data.local.dao

import androidx.room.*
import com.capstone.cropcare.data.local.entity.ScanResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanResult(scanResult: ScanResultEntity)

    @Update
    suspend fun updateScanResult(scanResult: ScanResultEntity)

    @Query("SELECT * FROM scan_results WHERE scanId = :scanResultId")
    suspend fun getScanResultById(scanResultId: String): ScanResultEntity?

    @Query("SELECT * FROM scan_results WHERE sessionId = :sessionId ORDER BY scannedAt DESC")
    fun getScanResultsBySession(sessionId: String): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE sessionId = :sessionId AND hasPlague = 1 ORDER BY scannedAt DESC")
    fun getPlagueResultsBySession(sessionId: String): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE hasPlague = 0 ORDER BY scannedAt DESC")
    fun getAllHealthyResults(): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE hasPlague = 1 ORDER BY scannedAt DESC")
    fun getAllPlagueResults(): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE syncedWithBackend = 0")
    suspend fun getUnsyncedScanResults(): List<ScanResultEntity>

    @Query("SELECT COUNT(*) FROM scan_results WHERE sessionId = :sessionId")
    suspend fun getCountBySession(sessionId: String): Int

    @Query("SELECT COUNT(*) FROM scan_results WHERE sessionId = :sessionId AND hasPlague = 1")
    suspend fun getPlagueCountBySession(sessionId: String): Int

    @Delete
    suspend fun deleteScanResult(scanResult: ScanResultEntity)

    @Query("DELETE FROM scan_results WHERE sessionId = :sessionId")
    suspend fun deleteScanResultsBySession(sessionId: String)

    @Query("DELETE FROM scan_results")
    suspend fun deleteAll()
}