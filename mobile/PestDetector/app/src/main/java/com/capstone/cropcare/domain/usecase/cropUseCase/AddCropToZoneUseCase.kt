package com.capstone.cropcare.domain.usecase.cropUseCase

import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import javax.inject.Inject

class AddCropToZoneUseCase @Inject constructor(
    private val cropZoneRepository: CropZoneRepository
) {
    suspend operator fun invoke(cropName: String, zoneId: String): Result<CropModel> {
        if (cropName.isBlank()) {
            return Result.failure(Exception("El nombre del cultivo es requerido"))
        }

        if (cropName.length < 3) {
            return Result.failure(Exception("El nombre debe tener al menos 3 caracteres"))
        }

        val crop = CropModel(
            id = "crop_${System.currentTimeMillis()}",
            name = cropName.trim(),
            zoneId = zoneId
        )

        return try {
            cropZoneRepository.insertCrop(crop)
            Result.success(crop)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}