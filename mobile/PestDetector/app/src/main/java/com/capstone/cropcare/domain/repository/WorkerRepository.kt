package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.model.WorkerModel
import com.capstone.cropcare.domain.model.ZoneModel
import kotlinx.coroutines.flow.Flow

interface WorkersRepository {
    suspend fun getWorkers(): Flow<List<WorkerModel>>
    suspend fun deleteWorker(workerId: String): Result<Unit>
    suspend fun updateWorkerPermissions(workerId: String, canManagePlots: Boolean): Result<Unit>

    // ========== GESTIÓN DE ZONAS ==========
    suspend fun assignZonesToWorker(workerId: String, zoneIds: List<String>): Result<Unit>
    fun getWorkerAssignedZones(workerId: String): Flow<List<ZoneModel>>
    suspend fun getWorkerAssignedZoneIds(workerId: String): List<String>
}