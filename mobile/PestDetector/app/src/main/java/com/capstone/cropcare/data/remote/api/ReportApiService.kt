package com.capstone.cropcare.data.remote.api

import com.capstone.cropcare.data.remote.dto.MetricsResponseDTO
import com.capstone.cropcare.data.remote.dto.SessionReportDTO
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ReportApiService {

    /**
     * Obtiene todos los reportes de sesiones de la empresa
     */
    @GET("api/reports/session-reports/")
    suspend fun getSessionReports(): Response<List<SessionReportDTO>>

    /**
     * Obtiene un reporte específico por ID
     */
    @GET("api/reports/session-reports/{id}/")
    suspend fun getSessionReport(
        @Path("id") reportId: Int
    ): Response<SessionReportDTO>

    /**
     * Obtiene reportes filtrados por zona
     */
    @GET("api/reports/session-reports/")
    suspend fun getSessionReportsByZona(
        @Query("zona") zonaId: Int
    ): Response<List<SessionReportDTO>>

    /**
     * Obtiene reportes filtrados por cultivo
     */
    @GET("api/reports/session-reports/")
    suspend fun getSessionReportsByCultivo(
        @Query("cultivo") cultivoId: Int
    ): Response<List<SessionReportDTO>>

    @GET("api/reports/session-reports/metrics/")
    suspend fun getMetrics(
        @Query("period") period: String = "month",
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("cultivo") cultivoId: Int? = null,
        @Query("zona") zonaId: Int? = null
    ): Response<MetricsResponseDTO>

    @GET("api/reports/session-reports/export_pdf/")
    suspend fun exportPdf(
        @Query("period") period: String = "month",
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<ResponseBody>
}