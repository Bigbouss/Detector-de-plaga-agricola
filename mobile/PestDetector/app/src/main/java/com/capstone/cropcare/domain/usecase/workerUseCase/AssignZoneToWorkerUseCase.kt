package com.capstone.cropcare.domain.usecase.workerUseCase


import com.capstone.cropcare.domain.repository.WorkersRepository
import javax.inject.Inject

class AssignZonesToWorkerUseCase @Inject constructor(
    private val workersRepository: WorkersRepository
) {
    suspend operator fun invoke(workerId: String, zoneIds: List<String>): Result<Unit> {
        return workersRepository.assignZonesToWorker(workerId, zoneIds)
    }
}