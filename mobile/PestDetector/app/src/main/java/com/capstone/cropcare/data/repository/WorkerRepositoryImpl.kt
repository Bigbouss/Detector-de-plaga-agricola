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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkersRepositoryImpl @Inject constructor(
    private val apiService: WorkersApiService,
    private val assignmentDao: WorkerZoneAssignmentDao,
    private val zoneDao: ZoneDao
) : WorkersRepository {

    override suspend fun getWorkers(): Flow<List<WorkerModel>> = flow {
        try {
            val response = apiService.getWorkers()

            if (response.isSuccessful && response.body() != null) {
                val workersFromApi = response.body()!!.map { it.toDomain() }

                // Enriquecer cada worker con sus zonas asignadas desde Room
                val workersWithZones = workersFromApi.map { worker ->
                    val assignedZoneIds = assignmentDao.getZoneIdsForWorker(worker.id)
                    worker.copy(assignedZoneIds = assignedZoneIds)
                }

                emit(workersWithZones)
                Log.d("WorkersRepository", "✅ ${workersWithZones.size} trabajadores obtenidos")
            } else {
                Log.e("WorkersRepository", "❌ Error obteniendo trabajadores")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("WorkersRepository", "❌ Exception obteniendo trabajadores", e)
            emit(emptyList())
        }
    }

    override suspend fun deleteWorker(workerId: String): Result<Unit> {
        return try {
            val id = workerId.toIntOrNull()
                ?: return Result.failure(Exception("ID inválido"))

            val response = apiService.deleteWorker(id)

            if (response.isSuccessful) {
                // Eliminar asignaciones locales también
                assignmentDao.deleteAssignmentsForWorker(workerId)
                Log.d("WorkersRepository", "✅ Trabajador eliminado: $workerId")
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al eliminar"
                Log.e("WorkersRepository", "❌ Error: $errorMsg")
                Result.failure(Exception("No se pudo eliminar el trabajador"))
            }
        } catch (e: Exception) {
            Log.e("WorkersRepository", "❌ Exception eliminando trabajador", e)
            Result.failure(Exception("Error de conexión"))
        }
    }

    override suspend fun updateWorkerPermissions(
        workerId: String,
        canManagePlots: Boolean
    ): Result<Unit> {
        // TODO: Implementar cuando el backend tenga este endpoint
        return Result.failure(Exception("Funcionalidad no disponible aún"))
    }

    // En WorkersRepositoryImpl, cambia esto:
    override suspend fun assignZonesToWorker(
        workerId: String,
        zoneIds: List<String>
    ): Result<Unit> {
        return try {
            val workerIdInt = workerId.toIntOrNull()
                ?: return Result.failure(Exception("ID de trabajador inválido"))

            val zoneIdsInt = zoneIds.mapNotNull { it.toIntOrNull() }

            // ✅ USA assignZonesToWorker en lugar de updateWorkerZones
            val response = apiService.assignZonesToWorker(
                workerIdInt,
                AssignZonesRequest(workerIdInt, zoneIdsInt)
            )

            if (response.isSuccessful) {
                // Actualizar Room: eliminar asignaciones anteriores e insertar nuevas
                assignmentDao.deleteAssignmentsForWorker(workerId)

                val assignments = zoneIds.map { zoneId ->
                    WorkerZoneAssignmentEntity(
                        workerId = workerId,
                        zoneId = zoneId
                    )
                }
                assignmentDao.insertAssignments(assignments)

                Log.d("WorkersRepository", "✅ Zonas asignadas a trabajador $workerId")
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al asignar zonas"
                Log.e("WorkersRepository", "❌ Error: $errorMsg")
                Result.failure(Exception("No se pudieron asignar las zonas"))
            }
        } catch (e: Exception) {
            Log.e("WorkersRepository", "❌ Exception asignando zonas", e)
            Result.failure(Exception("Error de conexión"))
        }
    }



    override fun getWorkerAssignedZones(workerId: String): Flow<List<ZoneModel>> {
        return assignmentDao.getAssignedZoneIds(workerId).map { assignments ->
            val zoneIds = assignments.map { it.zoneId }

            // Obtener todas las zonas y filtrar
            val allZones = zoneDao.getAllZones().first()

            // Filtrar solo las zonas asignadas y convertir a dominio
            allZones
                .filter { zoneEntity -> zoneIds.contains(zoneEntity.zoneId) }
                .map { zoneEntity -> zoneEntity.toDomain() }
        }
    }

    override suspend fun getWorkerAssignedZoneIds(workerId: String): List<String> {
        return assignmentDao.getZoneIdsForWorker(workerId)
    }
}