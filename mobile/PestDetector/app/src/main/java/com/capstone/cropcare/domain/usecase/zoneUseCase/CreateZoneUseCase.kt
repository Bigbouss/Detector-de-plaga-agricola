package com.capstone.cropcare.domain.usecase.zoneUseCase

import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import javax.inject.Inject

class CreateZoneUseCase @Inject constructor(
    private val cropZoneRepository: CropZoneRepository
) {
    suspend operator fun invoke(name: String, description: String?): Result<ZoneModel> {
        if (name.isBlank()) {
            return Result.failure(Exception("El nombre de la zona es requerido"))
        }

        if (name.length < 3) {
            return Result.failure(Exception("El nombre debe tener al menos 3 caracteres"))
        }

        val zone = ZoneModel(
            id = "zone_${System.currentTimeMillis()}",
            name = name.trim(),
            description = description?.trim()
        )

        return try {
            cropZoneRepository.insertZone(zone)
            Result.success(zone)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}