package com.capstone.cropcare.domain.usecase.zoneUseCase

import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import javax.inject.Inject

class DeleteZoneUseCase @Inject constructor(
    private val repository: CropZoneRepository
) {
    suspend operator fun invoke(zone: ZoneModel): Result<Unit> {
        // ✅ Llamar al backend para eliminar
        return repository.deleteZoneFromBackend(zone.id)
    }
}