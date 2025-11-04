package com.capstone.cropcare.domain.usecase.workerUseCase


import com.capstone.cropcare.domain.model.WorkerModel
import com.capstone.cropcare.domain.repository.WorkersRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkersUseCase @Inject constructor(
    private val workersRepository: WorkersRepository
) {
    suspend operator fun invoke(): Flow<List<WorkerModel>> {
        return workersRepository.getWorkers()
    }
}

class DeleteWorkerUseCase @Inject constructor(
    private val workersRepository: WorkersRepository
) {
    suspend operator fun invoke(workerId: String): Result<Unit> {
        if (workerId.isBlank()) {
            return Result.failure(Exception("ID inválido"))
        }
        return workersRepository.deleteWorker(workerId)
    }
}

class UpdateWorkerPermissionsUseCase @Inject constructor(
    private val workersRepository: WorkersRepository
) {
    suspend operator fun invoke(workerId: String, canManagePlots: Boolean): Result<Unit> {
        if (workerId.isBlank()) {
            return Result.failure(Exception("ID inválido"))
        }
        return workersRepository.updateWorkerPermissions(workerId, canManagePlots)
    }
}