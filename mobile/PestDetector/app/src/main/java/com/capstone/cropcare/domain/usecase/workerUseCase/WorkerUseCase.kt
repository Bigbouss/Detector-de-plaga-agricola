package com.capstone.cropcare.domain.usecase.workerUseCase

import com.capstone.cropcare.domain.model.WorkerModel
import com.capstone.cropcare.domain.repository.WorkersRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetWorkersUseCase @Inject constructor(
    private val workersRepository: WorkersRepository
) {
    suspend operator fun invoke(): Flow<List<WorkerModel>> {
        return workersRepository.getAllWorkers()
    }
}


class DeleteWorkerUseCase @Inject constructor(
    private val workersRepository: WorkersRepository
) {
    suspend operator fun invoke(workerId: Int): Result<Unit> {
        if (workerId <= 0) {
            return Result.failure(Exception("ID de trabajador inválido"))
        }

        return Result.failure(Exception("Funcionalidad no disponible aún"))
    }
}

class UpdateWorkerPermissionsUseCase @Inject constructor(
    private val workersRepository: WorkersRepository
) {
    suspend operator fun invoke(workerId: Int, canManagePlots: Boolean): Result<Unit> {
        if (workerId <= 0) {
            return Result.failure(Exception("ID de trabajador inválido"))
        }

        return workersRepository.updateWorkerPermissions(workerId, canManagePlots)
    }
}