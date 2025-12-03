package com.capstone.cropcare.data.remote.api

import com.capstone.cropcare.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ScannerApiService {

    // ==================== SESSION ENDPOINTS ====================

    /**
     * Obtiene todas las sesiones del worker
     */
    @GET("api/scanners/sessions/my_sessions/")
    suspend fun getMySessions(): Response<List<ScanSessionDTO>>

    /**
     * Crea una nueva sesión
     */
    @POST("api/scanners/sessions/")
    suspend fun createSession(
        @Body session: ScanSessionCreateDTO
    ): Response<ScanSessionDTO>

    /**
     * Actualiza una sesión existente
     */
    @PATCH("api/scanners/sessions/{session_id}/")
    suspend fun updateSession(
        @Path("session_id") sessionId: String,
        @Body update: ScanSessionUpdateDTO
    ): Response<ScanSessionDTO>

    /**
     * Obtiene una sesión específica
     */
    @GET("api/scanners/sessions/{session_id}/")
    suspend fun getSession(
        @Path("session_id") sessionId: String
    ): Response<ScanSessionDTO>

    /**
     * Finaliza una sesión
     */
    @POST("api/scanners/sessions/{session_id}/finish/")
    suspend fun finishSession(
        @Path("session_id") sessionId: String,
        @Body notes: Map<String, String> = emptyMap()
    ): Response<ScanSessionDTO>

    /**
     * Cancela una sesión
     */
    @POST("api/scanners/sessions/{session_id}/cancel/")
    suspend fun cancelSession(
        @Path("session_id") sessionId: String
    ): Response<ScanSessionDTO>

    /**
     * Sincroniza múltiples sesiones
     */
    @POST("api/scanners/sessions/sync/")
    suspend fun syncSessions(
        @Body request: SessionSyncRequest
    ): Response<SyncResponse>

    // ==================== SCAN RESULT ENDPOINTS ====================

    /**
     * Crea un nuevo resultado de escaneo
     */
    @POST("api/scanners/results/")
    suspend fun createScanResult(
        @Body result: ScanResultCreateDTO
    ): Response<ScanResultDTO>

    /**
     * Obtiene los resultados de una sesión
     */
    @GET("api/scanners/results/by_session/")
    suspend fun getScanResultsBySession(
        @Query("session_id") sessionId: String
    ): Response<List<ScanResultDTO>>

    /**
     * Sincroniza múltiples resultados
     */
    @POST("api/scanners/results/sync/")
    suspend fun syncScanResults(
        @Body request: ScanResultSyncRequest
    ): Response<SyncResponse>

    /**
     * Obtiene sesiones completadas con sus reportes
     */
    @GET("api/scanners/sessions/with_reports/")
    suspend fun getSessionsWithReports(
        @Query("worker") workerName: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<List<SessionWithReportDTO>>

    @Multipart
    @POST("scanners/results/{result_id}/upload_image/")
    suspend fun uploadScanImage(
        @Path("result_id") resultId: String,
        @Part image: MultipartBody.Part
    ): Response<ScanResultDTO>




}