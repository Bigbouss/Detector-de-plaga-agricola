package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.model.WorkerModel
import com.capstone.cropcare.domain.model.ZoneModel
import kotlinx.coroutines.flow.Flow

interface WorkersRepository {

    suspend fun getAllWorkers(): Flow<List<WorkerModel>>

    fun getWorkerAssignedZones(workerId: Int): Flow<List<ZoneModel>>

    suspend fun assignZonesToWorker(workerId: Int, zoneIds: List<Int>): Result<Unit>

    suspend fun updateWorkerPermissions(workerId: Int, canManagePlots: Boolean): Result<Unit>
}