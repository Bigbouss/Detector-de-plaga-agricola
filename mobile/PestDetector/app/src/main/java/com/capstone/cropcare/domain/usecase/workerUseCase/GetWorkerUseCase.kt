//package com.capstone.cropcare.domain.usecase.workerUseCase
//
//
//import com.capstone.cropcare.domain.model.UserModel
//import com.capstone.cropcare.domain.repository.WorkerRepository
//import kotlinx.coroutines.flow.Flow
//import javax.inject.Inject
//
//class GetWorkersUseCase @Inject constructor(
//    private val repository: WorkerRepository
//) {
//    suspend operator fun invoke(): Flow<List<UserModel>> {
//        return repository.getWorkers()
//    }
//}