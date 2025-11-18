//package com.capstone.cropcare.data.repository
//
//import android.util.Log
//import com.capstone.cropcare.data.local.dao.ScanResultDao
//import com.capstone.cropcare.domain.mappers.toEntity
//import com.capstone.cropcare.domain.mappers.toDomain
//import com.capstone.cropcare.domain.model.ScanResultModel
//import com.capstone.cropcare.domain.repository.ScanResultRepository
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.map
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class ScanResultRepositoryImpl @Inject constructor(
//    private val scanResultDao: ScanResultDao
//    // TODO: Agregar API service cuando tengas el endpoint en el backend
//) : ScanResultRepository {
//
//    override suspend fun saveScanResult(scanResult: ScanResultModel): Result<ScanResultModel> {
//        return try {
//            scanResultDao.insertScanResult(scanResult.toEntity())
//            Log.d("ScanResultRepo", "✅ Resultado de escaneo guardado: ${scanResult.id}")
//            Result.success(scanResult)
//        } catch (e: Exception) {
//            Log.e("ScanResultRepo", "❌ Error guardando resultado", e)
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun getScanResultById(scanResultId: String): ScanResultModel? {
//        return scanResultDao.getScanResultById(scanResultId)?.toDomain()
//    }
//
//    override suspend fun updateScanResult(scanResult: ScanResultModel): Result<Unit> {
//        return try {
//            scanResultDao.updateScanResult(scanResult.toEntity())
//            Log.d("ScanResultRepo", "✅ Resultado actualizado: ${scanResult.id}")
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Log.e("ScanResultRepo", "❌ Error actualizando resultado", e)
//            Result.failure(e)
//        }
//    }
//
//    override fun getScanResultsBySession(sessionId: String): Flow<List<ScanResultModel>> {
//        return scanResultDao.getScanResultsBySession(sessionId).map { entities ->
//            entities.map { it.toDomain() }
//        }
//    }
//
//    override fun getPlagueResultsBySession(sessionId: String): Flow<List<ScanResultModel>> {
//        return scanResultDao.getPlagueResultsBySession(sessionId).map { entities ->
//            entities.map { it.toDomain() }
//        }
//    }
//
//    override suspend fun deleteScanResult(scanResultId: String): Result<Unit> {
//        return try {
//            val result = scanResultDao.getScanResultById(scanResultId)
//            if (result != null) {
//                scanResultDao.deleteScanResult(result)
//                Log.d("ScanResultRepo", "✅ Resultado eliminado: $scanResultId")
//                Result.success(Unit)
//            } else {
//                Result.failure(Exception("Resultado no encontrado"))
//            }
//        } catch (e: Exception) {
//            Log.e("ScanResultRepo", "❌ Error eliminando resultado", e)
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun syncScanResultsWithBackend(): Result<Unit> {
//        // TODO: Implementar cuando tengas el endpoint en el backend
//        return Result.success(Unit)
//    }
//
//    override suspend fun getUnsyncedScanResults(): List<ScanResultModel> {
//        return scanResultDao.getUnsyncedScanResults().map { it.toDomain() }
//    }
//}