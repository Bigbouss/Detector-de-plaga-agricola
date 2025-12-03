package com.capstone.cropcare.domain.usecase.cropUseCase

import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import javax.inject.Inject

class DeleteCropUseCase @Inject constructor(
    private val repository: CropZoneRepository
) {
    suspend operator fun invoke(crop: CropModel): Result<Unit> {

        return repository.deleteCropFromBackend(crop.id)
    }
}