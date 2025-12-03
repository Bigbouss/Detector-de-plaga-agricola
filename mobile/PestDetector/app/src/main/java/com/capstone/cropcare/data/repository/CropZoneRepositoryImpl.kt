package com.capstone.cropcare.data.repository

import android.util.Log
import com.capstone.cropcare.data.local.dao.CropDao
import com.capstone.cropcare.data.local.dao.ZoneDao
import com.capstone.cropcare.data.remote.api.WorkersApiService
import com.capstone.cropcare.data.remote.api.ZonesApiService
import com.capstone.cropcare.data.remote.dto.CreateCropRequest
import com.capstone.cropcare.data.remote.dto.CreateZoneRequest
import com.capstone.cropcare.domain.mappers.toDomain
import com.capstone.cropcare.domain.mappers.toDomainCrops
import com.capstone.cropcare.domain.mappers.toDomainZone
import com.capstone.cropcare.domain.mappers.toEntity
import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.AuthRepository
import com.capstone.cropcare.domain.repository.CropZoneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropZoneRepositoryImpl @Inject constructor(
    private val zoneDao: ZoneDao,
    private val cropDao: CropDao,
    private val zonesApi: ZonesApiService,
    private val workersApi: WorkersApiService,
    private val authRepository: AuthRepository
) : CropZoneRepository {

    // ==================== ZONAS - LOCAL ====================

    override suspend fun insertZone(zone: ZoneModel) {
        zoneDao.insertZone(zone.toEntity())
    }

    override suspend fun updateZone(zone: ZoneModel) {
        zoneDao.insertZone(zone.toEntity())
    }

    override suspend fun deleteZone(zoneId: String) {
        zoneDao.getZoneById(zoneId)?.let { entity ->
            zoneDao.deleteZone(entity)
        }
    }

    override fun getAllZones(): Flow<List<ZoneModel>> {
        return zoneDao.getAllZones().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getZoneById(zoneId: String): ZoneModel? {
        return zoneDao.getZoneById(zoneId)?.toDomain()
    }

    // ==================== ZONAS - BACKEND ====================

    override suspend fun createZone(name: String, description: String?): Result<ZoneModel> {
        return try {
            val request = CreateZoneRequest(
                nombre = name,
                descripcion = description
            )

            Log.d("CropZoneRepo", "Creando zona en backend: $name")
            val response = zonesApi.createZone(request)

            if (response.isSuccessful && response.body() != null) {
                val zoneDto = response.body()!!
                val zone = zoneDto.toDomainZone()

                // Guardar en Room también para tener cache local
                zoneDao.insertZone(zone.toEntity())

                Log.d("CropZoneRepo", "Zona creada exitosamente: ${zone.name} (ID: ${zone.id})")
                Result.success(zone)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("CropZoneRepo", "Error al crear zona: $errorMsg")
                Result.failure(Exception("No se pudo crear la zona: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("CropZoneRepo", "Exception creando zona", e)
            Result.failure(Exception("Error de conexión al crear zona: ${e.message}"))
        }
    }

    override suspend fun deleteZoneFromBackend(zoneId: String): Result<Unit> {
        return try {
            val zoneIdInt = zoneId.toIntOrNull()
                ?: return Result.failure(Exception("ID de zona inválido"))

            Log.d("CropZoneRepo", "Eliminando zona del backend: $zoneId")
            val response = zonesApi.deleteZone(zoneIdInt)

            if (response.isSuccessful) {
                // Eliminar de Room también
                deleteZone(zoneId)

                Log.d("CropZoneRepo", "Zona eliminada exitosamente: $zoneId")
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("CropZoneRepo", "Error al eliminar zona: $errorMsg")
                Result.failure(Exception("No se pudo eliminar la zona: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("CropZoneRepo", "Exception eliminando zona", e)
            Result.failure(Exception("Error de conexión al eliminar zona: ${e.message}"))
        }
    }

    // ==================== ZONAS - SINCRONIZACIÓN ====================

    override suspend fun syncAllZonesFromBackend(): Result<Unit> {
        return try {
            Log.d("CropZoneRepo", "Sincronizando todas las zonas desde backend...")

            val response = zonesApi.getZones()

            if (response.isSuccessful && response.body() != null) {
                val zonesFromBackend = response.body()!!

                // Convertir a domain models
                val zones = zonesFromBackend.map { it.toDomainZone() }
                val allCrops = mutableListOf<CropModel>()

                // Recolectar todos los cultivos
                zonesFromBackend.forEach { zoneResponse ->
                    allCrops.addAll(zoneResponse.toDomainCrops())
                }

                cropDao.deleteAll()  // Primero cultivos (por la FK)
                zoneDao.deleteAll()  // Luego zonas

                if (zones.isNotEmpty()) {
                    zoneDao.insertZones(zones.map { it.toEntity() })
                    Log.d("CropZoneRepo", "${zones.size} zonas insertadas")
                }

                if (allCrops.isNotEmpty()) {
                    cropDao.insertCrops(allCrops.map { it.toEntity() })
                    Log.d("CropZoneRepo", "${allCrops.size} cultivos insertados")
                }

                Log.d("CropZoneRepo", "Sincronización completa: ${zones.size} zonas, ${allCrops.size} cultivos")
                Result.success(Unit)
            } else {
                val errorMsg = "Error al sincronizar: ${response.code()}"
                Log.e("CropZoneRepo", " $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("CropZoneRepo", "Error sincronizando zonas", e)
            Result.failure(e)
        }
    }

    override suspend fun getAssignedZonesForCurrentWorker(): Result<List<ZoneModel>> {
        return try {
            val currentUser = authRepository.getCurrentUser()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val workerId = currentUser.id

            Log.d("CropZoneRepo", "Obteniendo zonas para worker $workerId")

            val response = workersApi.getWorkerAssignedZones(workerId)

            if (response.isSuccessful && response.body() != null) {
                val zoneIds = response.body()!!.zoneIds
                Log.d("CropZoneRepo", "Zonas asignadas: $zoneIds")

                if (zoneIds.isEmpty()) {
                    Log.w("CropZoneRepo", "⚠Worker no tiene zonas asignadas")
                    return Result.success(emptyList())
                }

                // Obtener detalles de las zonas
                val zonesResponse = zonesApi.getZones()

                if (zonesResponse.isSuccessful && zonesResponse.body() != null) {
                    val allZones = zonesResponse.body()!!
                    val assignedZones = allZones
                        .filter { it.id in zoneIds }
                        .map { it.toDomainZone() }

                    Log.d("CropZoneRepo", " ${assignedZones.size} zonas cargadas: ${assignedZones.map { it.name }}")
                    Result.success(assignedZones)
                } else {
                    val errorMsg = "Error obteniendo detalles de zonas: ${zonesResponse.code()}"
                    Log.e("CropZoneRepo", " $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "Error obteniendo zonas asignadas: ${response.code()}"
                Log.e("CropZoneRepo", " $errorMsg")

                if (response.code() == 403) {
                    Result.failure(Exception("No tienes permisos. Contacta al administrador."))
                } else {
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Log.e("CropZoneRepo", " Exception obteniendo zonas asignadas", e)
            Result.failure(e)
        }
    }

    override suspend fun syncAssignedZonesFromBackend(): Result<Unit> {
        return try {
            val result = getAssignedZonesForCurrentWorker()

            if (result.isSuccess) {
                val zones = result.getOrNull() ?: emptyList()
                zoneDao.deleteAll()

                if (zones.isNotEmpty()) {
                    zoneDao.insertZones(zones.map { it.toEntity() })
                }

                Log.d("CropZoneRepo", " ${zones.size} zonas sincronizadas")
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e("CropZoneRepo", " Error sincronizando zonas", e)
            Result.failure(e)
        }
    }

    // ==================== CULTIVOS - LOCAL ====================

    override suspend fun insertCrop(crop: CropModel) {
        cropDao.insertCrop(crop.toEntity())
    }

    override suspend fun updateCrop(crop: CropModel) {
        cropDao.insertCrop(crop.toEntity())
    }

    override suspend fun deleteCrop(cropId: String) {
        cropDao.getCropById(cropId)?.let { entity ->
            cropDao.deleteCrop(entity)
        }
    }

    override fun getCropsByZone(zoneId: String): Flow<List<CropModel>> {
        return cropDao.getCropsByZone(zoneId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCropById(cropId: String): CropModel? {
        return cropDao.getCropById(cropId)?.toDomain()
    }

    // ==================== CULTIVOS - BACKEND ====================

    override suspend fun createCrop(name: String, zoneId: String): Result<CropModel> {
        return try {
            val zoneIdInt = zoneId.toIntOrNull()
                ?: return Result.failure(Exception("ID de zona inválido"))

            val request = CreateCropRequest(
                nombre = name,
                zona = zoneIdInt
            )

            Log.d("CropZoneRepo", "Creando cultivo en backend: $name para zona $zoneId")
            val response = zonesApi.createCrop(request)

            if (response.isSuccessful && response.body() != null) {
                val cropDto = response.body()!!
                val crop = cropDto.toDomain()

                // Guardar en Room también
                cropDao.insertCrop(crop.toEntity())

                Log.d("CropZoneRepo", "Cultivo creado exitosamente: ${crop.name} (ID: ${crop.id})")
                Result.success(crop)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("CropZoneRepo", "Error al crear cultivo: $errorMsg")
                Result.failure(Exception("No se pudo crear el cultivo: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("CropZoneRepo", "Exception creando cultivo", e)
            Result.failure(Exception("Error de conexión al crear cultivo: ${e.message}"))
        }
    }

    override suspend fun deleteCropFromBackend(cropId: String): Result<Unit> {
        return try {
            val cropIdInt = cropId.toIntOrNull()
                ?: return Result.failure(Exception("ID de cultivo inválido"))

            Log.d("CropZoneRepo", "Eliminando cultivo del backend: $cropId")
            val response = zonesApi.deleteCrop(cropIdInt)

            if (response.isSuccessful) {
                // Eliminar de Room también
                deleteCrop(cropId)

                Log.d("CropZoneRepo", "Cultivo eliminado exitosamente: $cropId")
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("CropZoneRepo", "Error al eliminar cultivo: $errorMsg")
                Result.failure(Exception("No se pudo eliminar el cultivo: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("CropZoneRepo", "Exception eliminando cultivo", e)
            Result.failure(Exception("Error de conexión al eliminar cultivo: ${e.message}"))
        }
    }

    // ==================== CULTIVOS - SINCRONIZACIÓN ====================

    override suspend fun syncCropsForZone(zoneId: String): Result<Unit> {
        return try {
            val zoneIdInt = zoneId.toIntOrNull()
                ?: return Result.failure(Exception("ID de zona inválido"))

            val response = zonesApi.getZones()

            if (response.isSuccessful && response.body() != null) {
                val zone = response.body()!!.find { it.id == zoneIdInt }

                if (zone != null) {
                    cropDao.deleteCropsByZone(zoneId)

                    val crops = zone.toDomainCrops()
                    if (crops.isNotEmpty()) {
                        cropDao.insertCrops(crops.map { it.toEntity() })
                    }

                    Log.d("CropZoneRepo", " ${crops.size} cultivos sincronizados para zona $zoneId")
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Zona no encontrada"))
                }
            } else {
                Result.failure(Exception("Error al obtener cultivos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CropZoneRepo", "Error sincronizando cultivos", e)
            Result.failure(e)
        }
    }
}