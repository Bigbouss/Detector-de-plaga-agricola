package com.capstone.cropcare.data.repository

import android.util.Log
import com.capstone.cropcare.data.local.dao.WorkerZoneAssignmentDao
import com.capstone.cropcare.data.local.dao.ZoneDao
import com.capstone.cropcare.data.local.entity.WorkerZoneAssignmentEntity
import com.capstone.cropcare.data.remote.api.WorkersApiService
import com.capstone.cropcare.data.remote.dto.AssignZonesRequest
import com.capstone.cropcare.domain.mappers.toDomain
import com.capstone.cropcare.domain.model.WorkerModel
import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.WorkersRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkersRepositoryImpl @Inject constructor(
    private val apiService: WorkersApiService,
    private val assignmentDao: WorkerZoneAssignmentDao,
    private val zoneDao: ZoneDao
) : WorkersRepository {

    override suspend fun getAllWorkers(): Flow<List<WorkerModel>> = flow {
        try {
            val response = apiService.getWorkers()

            if (response.isSuccessful && response.body() != null) {
                val workersFromApi = response.body()!!.map { it.toDomain() }

                val workersWithZones = workersFromApi.map { worker ->
                    // Convertir Int a String para Room
                    val assignedZoneIds = assignmentDao.getZoneIdsForWorker(worker.id.toString())
                    worker.copy(assignedZoneIds = assignedZoneIds)
                }

                emit(workersWithZones)
                Log.d("WorkersRepository", "${workersWithZones.size} trabajadores obtenidos")
            } else {
                Log.e("WorkersRepository", "Error obteniendo trabajadores: ${response.code()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("WorkersRepository", "Exception obteniendo trabajadores", e)
            emit(emptyList())
        }
    }

    override fun getWorkerAssignedZones(workerId: Int): Flow<List<ZoneModel>> {
        val workerIdStr = workerId.toString()

        return assignmentDao.getAssignedZoneIds(workerIdStr)
            .flatMapLatest { assignments ->
                val zoneIds = assignments.map { it.zoneId }

                // Obtener zonas desde Room y filtrar
                zoneDao.getAllZones().map { allZones ->
                    allZones
                        .filter { zoneEntity -> zoneIds.contains(zoneEntity.zoneId) }
                        .map { zoneEntity -> zoneEntity.toDomain() }
                }
            }
    }

    override suspend fun assignZonesToWorker(workerId: Int, zoneIds: List<Int>): Result<Unit> {
        return try {
            Log.d("WorkersRepository", "Asignando ${zoneIds.size} zonas al worker $workerId")

            val response = apiService.assignZonesToWorker(
                AssignZonesRequest(workerId, zoneIds)
            )

            if (response.isSuccessful && response.body() != null) {
                // Actualizar Room con las asignaciones
                val workerIdStr = workerId.toString()
                assignmentDao.deleteAssignmentsForWorker(workerIdStr)

                val assignments = zoneIds.map { zoneId ->
                    WorkerZoneAssignmentEntity(
                        workerId = workerIdStr,
                        zoneId = zoneId.toString()
                    )
                }
                assignmentDao.insertAssignments(assignments)

                Log.d("WorkersRepository", "Zonas asignadas exitosamente")
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al asignar zonas"
                Log.e("WorkersRepository", "Error: $errorMsg")
                Result.failure(Exception("No se pudieron asignar las zonas"))
            }
        } catch (e: Exception) {
            Log.e("WorkersRepository", "Exception asignando zonas", e)
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    override suspend fun updateWorkerPermissions(
        workerId: Int,
        canManagePlots: Boolean
    ): Result<Unit> {
        Log.w("WorkersRepository", "updateWorkerPermissions no implementado en el backend")
        return Result.failure(Exception("Funcionalidad no disponible aún"))
    }
}