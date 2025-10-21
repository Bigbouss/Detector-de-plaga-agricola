package com.capstone.cropcare.domain.usecase.cropUseCase

import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCropsByZoneUseCase @Inject constructor(
    private val cropZoneRepository: CropZoneRepository
) {
    operator fun invoke(zoneId: String): Flow<List<CropModel>> {
        return cropZoneRepository.getCropsByZone(zoneId)
    }
}