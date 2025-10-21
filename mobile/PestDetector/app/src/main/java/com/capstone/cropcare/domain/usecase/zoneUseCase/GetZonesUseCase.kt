package com.capstone.cropcare.domain.usecase.zoneUseCase

import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetZonesUseCase @Inject constructor(
    private val cropZoneRepository: CropZoneRepository
) {
    operator fun invoke(): Flow<List<ZoneModel>> {
        return cropZoneRepository.getAllZones()
    }
}