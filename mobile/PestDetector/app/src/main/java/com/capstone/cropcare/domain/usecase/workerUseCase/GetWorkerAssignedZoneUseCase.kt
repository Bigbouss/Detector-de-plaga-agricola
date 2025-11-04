package com.capstone.cropcare.domain.usecase.workerUseCase

import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.WorkersRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkerAssignedZonesUseCase @Inject constructor(
    private val workersRepository: WorkersRepository
) {
    operator fun invoke(workerId: String): Flow<List<ZoneModel>> {
        return workersRepository.getWorkerAssignedZones(workerId)
    }
}