//package com.capstone.cropcare.data.repository
//
//package com.capstone.cropcare.data.repository
//
//import android.util.Log
//import com.capstone.cropcare.data.remote.api.WorkerApiService
//import com.capstone.cropcare.domain.mappers.toUserModel
//import com.capstone.cropcare.domain.model.UserModel
//import com.capstone.cropcare.domain.repository.WorkerRepository
//import com.capstone.cropcare.domain.usecase.authUseCase.GetCurrentUserUseCase
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flow
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class WorkerRepositoryImpl @Inject constructor(
//    private val apiService: WorkerApiService,
//    private val getCurrentUserUseCase: GetCurrentUserUseCase
//) : WorkerRepository {
//
//    override suspend fun getWorkers(): Flow<List<UserModel>> = flow {
//        try {
//            // Obtener info del admin actual para pasar organizationId y name
//            val currentUser = getCurrentUserUseCase()
//            if (currentUser == null) {
//                Log.e("WorkerRepository", "❌ Usuario no autenticado")
//                emit(emptyList())
//                return@flow
//            }
//
//            val response = apiService.getCompanyWorkers()
//
//            if (response.isSuccessful && response.body() != null) {
//                val workers = response.body()!!.map {
//                    it.toUserModel(
//                        organizationId = currentUser.organizationId,
//                        organizationName = currentUser.organizationName
//                    )
//                }
//                emit(workers)
//                Log.d("WorkerRepository", "✅ ${workers.size} workers obtenidos")
//            } else {
//                Log.e("WorkerRepository", "❌ Error obteniendo workers: ${response.code()}")
//                emit(emptyList())
//            }
//        } catch (e: Exception) {
//            Log.e("WorkerRepository", "❌ Exception obteniendo workers", e)
//            emit(emptyList())
//        }
//    }
//
//    override suspend fun deleteWorker(workerId: String): Result<Unit> {
//        return try {
//            val response = apiService.deleteWorker(workerId)
//
//            if (response.isSuccessful) {
//                Log.d("WorkerRepository", "✅ Worker eliminado: $workerId")
//                Result.success(Unit)
//            } else {
//                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
//                Log.e("WorkerRepository", "❌ Error eliminando worker: $errorMsg")
//                Result.failure(Exception("No se pudo eliminar el trabajador"))
//            }
//        } catch (e: Exception) {
//            Log.e("WorkerRepository", "❌ Exception eliminando worker", e)
//            Result.failure(Exception("Error de conexión"))
//        }
//    }
//}