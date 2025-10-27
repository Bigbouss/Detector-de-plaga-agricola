//package com.capstone.cropcare.domain.usecase.workerUseCase
//
//import com.capstone.cropcare.domain.repository.WorkerRepository
//import javax.inject.Inject
//
//class DeleteWorkerUseCase @Inject constructor(
//    private val repository: WorkerRepository
//) {
//    suspend operator fun invoke(workerId: String): Result<Unit> {
//        return repository.deleteWorker(workerId)
//    }
//}