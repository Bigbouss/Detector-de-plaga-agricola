package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.model.ScanResultModel
import kotlinx.coroutines.flow.Flow

interface ScanResultRepository {

    suspend fun saveScanResult(scanResult: ScanResultModel): Result<ScanResultModel>

    suspend fun getScanResultById(scanResultId: String): ScanResultModel?

    suspend fun updateScanResult(scanResult: ScanResultModel): Result<Unit>

    fun getScanResultsBySession(sessionId: String): Flow<List<ScanResultModel>>

    fun getPlagueResultsBySession(sessionId: String): Flow<List<ScanResultModel>>

    suspend fun deleteScanResult(scanResultId: String): Result<Unit>

    suspend fun syncScanResultsWithBackend(): Result<Unit>

    suspend fun getUnsyncedScanResults(): List<ScanResultModel>
}