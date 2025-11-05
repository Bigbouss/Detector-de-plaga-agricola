package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.model.ZoneModel
import kotlinx.coroutines.flow.Flow

interface CropZoneRepository {
    // Zonas - Local
    suspend fun insertZone(zone: ZoneModel)
    suspend fun updateZone(zone: ZoneModel)
    suspend fun deleteZone(zoneId: String)
    fun getAllZones(): Flow<List<ZoneModel>>
    suspend fun getZoneById(zoneId: String): ZoneModel?

    // Zonas - Backend
    suspend fun createZone(name: String, description: String?): Result<ZoneModel>
    suspend fun deleteZoneFromBackend(zoneId: String): Result<Unit>
    suspend fun syncAllZonesFromBackend(): Result<Unit>
    suspend fun getAssignedZonesForCurrentWorker(): Result<List<ZoneModel>>
    suspend fun syncAssignedZonesFromBackend(): Result<Unit>

    // Cultivos - Local
    suspend fun insertCrop(crop: CropModel)
    suspend fun updateCrop(crop: CropModel)
    suspend fun deleteCrop(cropId: String)
    fun getCropsByZone(zoneId: String): Flow<List<CropModel>>
    suspend fun getCropById(cropId: String): CropModel?

    // Cultivos - Backend
    suspend fun createCrop(name: String, zoneId: String): Result<CropModel>
    suspend fun deleteCropFromBackend(cropId: String): Result<Unit>
    suspend fun syncCropsForZone(zoneId: String): Result<Unit>
}
