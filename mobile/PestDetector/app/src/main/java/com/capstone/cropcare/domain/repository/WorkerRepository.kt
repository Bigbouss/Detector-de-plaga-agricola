//package com.capstone.cropcare.domain.repository
//
//import com.capstone.cropcare.domain.model.UserModel
//import kotlinx.coroutines.flow.Flow
//
//interface WorkerRepository {
//    suspend fun getWorkers(): Flow<List<UserModel>>
//    suspend fun deleteWorker(workerId: String): Result<Unit>
//}