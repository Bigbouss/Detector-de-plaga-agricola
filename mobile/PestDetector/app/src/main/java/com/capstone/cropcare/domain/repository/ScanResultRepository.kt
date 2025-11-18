//package com.capstone.cropcare.domain.repository
//
//import com.capstone.cropcare.domain.model.ScanResultModel
//import kotlinx.coroutines.flow.Flow
//
//interface ScanResultRepository {
//
//    /**
//     * Guarda un resultado de escaneo
//     */
//    suspend fun saveScanResult(scanResult: ScanResultModel): Result<ScanResultModel>
//
//    /**
//     * Obtiene un resultado de escaneo por ID
//     */
//    suspend fun getScanResultById(scanResultId: String): ScanResultModel?
//
//    /**
//     * Actualiza un resultado de escaneo
//     */
//    suspend fun updateScanResult(scanResult: ScanResultModel): Result<Unit>
//
//    /**
//     * Obtiene todos los resultados de una sesión
//     */
//    fun getScanResultsBySession(sessionId: String): Flow<List<ScanResultModel>>
//
//    /**
//     * Obtiene solo los resultados con plagas de una sesión
//     */
//    fun getPlagueResultsBySession(sessionId: String): Flow<List<ScanResultModel>>
//
//    /**
//     * Elimina un resultado de escaneo
//     */
//    suspend fun deleteScanResult(scanResultId: String): Result<Unit>
//
//    /**
//     * Sincroniza resultados con el backend
//     */
//    suspend fun syncScanResultsWithBackend(): Result<Unit>
//
//    /**
//     * Obtiene resultados no sincronizados
//     */
//    suspend fun getUnsyncedScanResults(): List<ScanResultModel>
//}