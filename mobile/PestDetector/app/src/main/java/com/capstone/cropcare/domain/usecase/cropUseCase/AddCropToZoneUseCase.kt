package com.capstone.cropcare.domain.usecase.cropUseCase

import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import javax.inject.Inject

class AddCropToZoneUseCase @Inject constructor(
    private val cropZoneRepository: CropZoneRepository
) {
    suspend operator fun invoke(cropName: String, zoneId: String): Result<CropModel> {
        // Validaciones
        if (cropName.isBlank()) {
            return Result.failure(Exception("El nombre del cultivo es requerido"))
        }

        if (cropName.length < 2) {
            return Result.failure(Exception("El nombre debe tener al menos 2 caracteres"))
        }

        // ✅ CAMBIO: Llamar al backend
        return cropZoneRepository.createCrop(
            name = cropName.trim(),
            zoneId = zoneId
        )
    }
}