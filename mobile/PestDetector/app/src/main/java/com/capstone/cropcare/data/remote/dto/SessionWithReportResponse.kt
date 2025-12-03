package com.capstone.cropcare.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que combina sesión con su reporte anidado
 */
data class SessionWithReportDTO(
    // Datos de la sesión
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("empresa") val empresaId: Int,
    @SerializedName("empresa_name") val empresaName: String,
    @SerializedName("zona") val zonaId: Int,
    @SerializedName("zona_name") val zonaName: String,
    @SerializedName("cultivo") val cultivoId: Int,
    @SerializedName("cultivo_name") val cultivoName: String,
    @SerializedName("owner") val ownerId: Int,
    @SerializedName("worker_name") val workerName: String,

    // Timestamps
    @SerializedName("started_at") val startedAt: String,
    @SerializedName("finished_at") val finishedAt: String?,
    @SerializedName("status") val status: String,

    // Resumen de escaneos
    @SerializedName("total_scans") val totalScans: Int,
    @SerializedName("healthy_count") val healthyCount: Int,
    @SerializedName("plague_count") val plagueCount: Int,
    @SerializedName("notes") val notes: String?,

    //Reporte anidado
    @SerializedName("report") val report: SessionReportDTO?,
    //Agregar scan_result
    @SerializedName("scan_results") val scanResults: List<ScanResultDTO>? = null,


)