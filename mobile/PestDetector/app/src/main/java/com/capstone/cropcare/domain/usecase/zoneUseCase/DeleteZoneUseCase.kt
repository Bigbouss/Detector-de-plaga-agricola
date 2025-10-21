package com.capstone.cropcare.domain.usecase.zoneUseCase

import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import javax.inject.Inject

class DeleteZoneUseCase @Inject constructor(
    private val cropZoneRepository: CropZoneRepository
) {
    suspend operator fun invoke(zone: ZoneModel): Result<Unit> {
        return try {
            cropZoneRepository.deleteZone(zone)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}