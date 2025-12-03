package com.capstone.cropcare.data.repository

import android.util.Log
import com.capstone.cropcare.data.local.dao.ScanResultDao
import com.capstone.cropcare.data.remote.api.ScannerApiService
import com.capstone.cropcare.domain.mappers.toCreateDTO
import com.capstone.cropcare.domain.mappers.toDomain
import com.capstone.cropcare.domain.mappers.toEntity
import com.capstone.cropcare.domain.mappers.toSyncRequest
import com.capstone.cropcare.domain.model.ScanResultModel
import com.capstone.cropcare.domain.repository.ScanResultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanResultRepositoryImpl @Inject constructor(
    private val scanResultDao: ScanResultDao,
    private val scannerApiService: ScannerApiService
) : ScanResultRepository {

    override suspend fun saveScanResult(scanResult: ScanResultModel): Result<ScanResultModel> {
        return try {
            // Guardar localmente
            scanResultDao.insertScanResult(scanResult.toEntity())
            Log.d("ScanResultRepo", "Resultado de escaneo guardado: ${scanResult.id}")

            // Intentar sincronizar con backend
            try {
                val dto = scanResult.toCreateDTO()
                val response = scannerApiService.createScanResult(dto)

                if (response.isSuccessful) {
                    // Marcar como sincronizado
                    val syncedResult = scanResult.copy(syncedWithBackend = true)
                    scanResultDao.updateScanResult(syncedResult.toEntity())
                    Log.d("ScanResultRepo", "Resultado sincronizado con backend: ${scanResult.id}")
                } else {
                    Log.w("ScanResultRepo", "No se pudo sincronizar resultado: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.w("ScanResultRepo", "Error sincronizando resultado con backend", e)
            }

            Result.success(scanResult)
        } catch (e: Exception) {
            Log.e("ScanResultRepo", "Error guardando resultado", e)
            Result.failure(e)
        }
    }

    override suspend fun getScanResultById(scanResultId: String): ScanResultModel? {
        return scanResultDao.getScanResultById(scanResultId)?.toDomain()
    }

    override suspend fun updateScanResult(scanResult: ScanResultModel): Result<Unit> {
        return try {
            scanResultDao.updateScanResult(scanResult.toEntity())
            Log.d("ScanResultRepo", "Resultado actualizado: ${scanResult.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ScanResultRepo", "Error actualizando resultado", e)
            Result.failure(e)
        }
    }

    override fun getScanResultsBySession(sessionId: String): Flow<List<ScanResultModel>> {
        return scanResultDao.getScanResultsBySession(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPlagueResultsBySession(sessionId: String): Flow<List<ScanResultModel>> {
        return scanResultDao.getPlagueResultsBySession(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteScanResult(scanResultId: String): Result<Unit> {
        return try {
            val result = scanResultDao.getScanResultById(scanResultId)
            if (result != null) {
                scanResultDao.deleteScanResult(result)
                Log.d("ScanResultRepo", "Resultado eliminado: $scanResultId")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Resultado no encontrado"))
            }
        } catch (e: Exception) {
            Log.e("ScanResultRepo", "Error eliminando resultado", e)
            Result.failure(e)
        }
    }

    // ==================== SINCRONIZACIÓN ====================

    override suspend fun syncScanResultsWithBackend(): Result<Unit> {
        return try {
            val unsyncedResults = getUnsyncedScanResults()

            if (unsyncedResults.isEmpty()) {
                Log.d("ScanResultRepo", "No hay resultados por sincronizar")
                return Result.success(Unit)
            }

            Log.d("ScanResultRepo", "Sincronizando ${unsyncedResults.size} resultados...")

            //Crear request de sincronización
            val syncRequest = unsyncedResults.toSyncRequest()

            //Enviar metadata al backend
            val response = scannerApiService.syncScanResults(syncRequest)

            if (response.isSuccessful) {
                val syncResponse = response.body()!!

                Log.d("ScanResultRepo", """
                    Metadata sincronizada:
                    - Resultados: ${syncResponse.totalSynced}
                    - Errores: ${syncResponse.totalErrors}
                """.trimIndent())

                val plagueResults = unsyncedResults.filter { it.hasPlague }
                if (plagueResults.isNotEmpty()) {
                    Log.d("ScanResultRepo", "Subiendo ${plagueResults.size} imágenes de plagas...")
                    uploadPlagueImages(plagueResults)
                } else {
                    Log.d("ScanResultRepo", "No hay imágenes de plagas para subir")
                }

                syncResponse.synced.forEach { syncedItem ->
                    syncedItem.resultId?.let { resultId ->
                        val result = getScanResultById(resultId)
                        result?.let {
                            val synced = it.copy(syncedWithBackend = true)
                            scanResultDao.updateScanResult(synced.toEntity())
                        }
                    }
                }

                Result.success(Unit)
            } else {
                Log.e("ScanResultRepo", "Error en sincronización: ${response.code()}")
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }

        } catch (e: Exception) {
            Log.e("ScanResultRepo", "Error sincronizando resultados", e)
            Result.failure(e)
        }
    }
    private suspend fun uploadPlagueImages(plagueResults: List<ScanResultModel>) {
        var uploadedCount = 0
        var failedCount = 0

        plagueResults.forEach { result ->
            if (result.photoPath.isNotEmpty()) {
                val uploadSuccess = uploadImageIfNeeded(result.id, result.photoPath)
                if (uploadSuccess) {
                    uploadedCount++
                } else {
                    failedCount++
                }
            }
        }

        Log.d("ScanResultRepo", """
            Resumen de subida de imágenes:
            - Exitosas: $uploadedCount
            - Fallidas: $failedCount
            - Total: ${plagueResults.size}
        """.trimIndent())
    }
    //ignorar warning :(
    private suspend fun uploadImageIfNeeded(resultId: String, photoPath: String): Boolean {
        return try {
            val file = File(photoPath)
            if (!file.exists()) {
                Log.w("ScanResultRepo", "Archivo no existe: $photoPath")
                return false
            }

            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val response = scannerApiService.uploadScanImage(resultId, body)

            if (response.isSuccessful) {
                Log.d("ScanResultRepo", "Imagen subida: $resultId")
                return true
            } else {
                Log.w("ScanResultRepo", "Error subiendo imagen: ${response.code()}")
                return false
            }

        } catch (e: Exception) {
            Log.e("ScanResultRepo", "Error upload imagen", e)
            return false
        }
    }

    override suspend fun getUnsyncedScanResults(): List<ScanResultModel> {
        return scanResultDao.getUnsyncedScanResults().map { it.toDomain() }
    }

    suspend fun fetchScanResultsFromBackend(sessionId: String): Result<List<ScanResultModel>> {
        return try {
            val response = scannerApiService.getScanResultsBySession(sessionId)

            if (response.isSuccessful) {
                val results = response.body()?.map { it.toDomain() } ?: emptyList()

                // Guardar en base de datos local
                results.forEach { result ->
                    scanResultDao.insertScanResult(result.toEntity())
                }

                Log.d("ScanResultRepo", "${results.size} resultados descargados del backend")
                Result.success(results)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ScanResultRepo", "Error descargando resultados", e)
            Result.failure(e)
        }
    }
}