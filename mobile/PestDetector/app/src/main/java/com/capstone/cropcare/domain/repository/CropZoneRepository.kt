package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.model.ZoneModel
import kotlinx.coroutines.flow.Flow

interface CropZoneRepository {
    fun getAllZones(): Flow<List<ZoneModel>>
    fun getCropsByZone(zoneId: String): Flow<List<CropModel>>
    suspend fun insertZone(zone: ZoneModel)
    suspend fun insertCrop(crop: CropModel)
    suspend fun deleteZone(zone: ZoneModel)
    suspend fun deleteCrop(crop: CropModel)
}