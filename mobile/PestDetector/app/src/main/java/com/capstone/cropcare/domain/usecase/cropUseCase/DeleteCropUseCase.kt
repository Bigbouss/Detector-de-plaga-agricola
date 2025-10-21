package com.capstone.cropcare.domain.usecase.cropUseCase

import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import javax.inject.Inject

class DeleteCropUseCase @Inject constructor(
    private val cropZoneRepository: CropZoneRepository
) {
    suspend operator fun invoke(crop: CropModel): Result<Unit> {
        return try {
            cropZoneRepository.deleteCrop(crop)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}