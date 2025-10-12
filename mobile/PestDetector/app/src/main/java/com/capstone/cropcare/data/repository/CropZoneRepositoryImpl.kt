package com.capstone.cropcare.data.repository

import com.capstone.cropcare.data.local.dao.CropDao
import com.capstone.cropcare.data.local.dao.ZoneDao
import com.capstone.cropcare.domain.mappers.toDomain
import com.capstone.cropcare.domain.mappers.toEntity
import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropZoneRepositoryImpl @Inject constructor(
    private val zoneDao: ZoneDao,
    private val cropDao: CropDao
) : CropZoneRepository {

    override fun getAllZones(): Flow<List<ZoneModel>> {
        return zoneDao.getAllZones().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCropsByZone(zoneId: String): Flow<List<CropModel>> {
        return cropDao.getCropsByZone(zoneId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertZone(zone: ZoneModel) {
        zoneDao.insertZone(zone.toEntity())
    }

    override suspend fun insertCrop(crop: CropModel) {
        cropDao.insertCrop(crop.toEntity())
    }

    override suspend fun deleteZone(zone: ZoneModel) {
        zoneDao.deleteZone(zone.toEntity())
    }

    override suspend fun deleteCrop(crop: CropModel) {
        cropDao.deleteCrop(crop.toEntity())
    }
}