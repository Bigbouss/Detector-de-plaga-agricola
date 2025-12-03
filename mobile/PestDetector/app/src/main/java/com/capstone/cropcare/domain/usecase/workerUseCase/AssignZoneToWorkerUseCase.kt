package com.capstone.cropcare.domain.usecase.workerUseCase

import com.capstone.cropcare.domain.repository.WorkersRepository
import javax.inject.Inject

class AssignZonesToWorkerUseCase @Inject constructor(
    private val workersRepository: WorkersRepository
) {
    suspend operator fun invoke(workerId: Int, zoneIds: List<Int>): Result<Unit> {
        // Validación básica
        if (zoneIds.isEmpty()) {
            return Result.failure(Exception("Debes seleccionar al menos una zona"))
        }

        return workersRepository.assignZonesToWorker(workerId, zoneIds)
    }
}