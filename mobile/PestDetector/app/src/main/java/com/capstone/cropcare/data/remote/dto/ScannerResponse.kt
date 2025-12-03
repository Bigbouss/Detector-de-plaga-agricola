package com.capstone.cropcare.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== SESSION DTOs ====================

data class ScanSessionDTO(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("empresa") val empresaId: Int,
    @SerializedName("empresa_name") val empresaName: String? = null,
    @SerializedName("zona") val zonaId: Int,
    @SerializedName("zona_name") val zonaName: String? = null,
    @SerializedName("cultivo") val cultivoId: Int,
    @SerializedName("cultivo_name") val cultivoName: String? = null,
    @SerializedName("owner") val ownerId: Int,
    @SerializedName("worker_name") val workerName: String,
    @SerializedName("model_version_string") val modelVersionString: String,
    @SerializedName("started_at") val startedAt: String,  // ISO 8601 format
    @SerializedName("finished_at") val finishedAt: String? = null,
    @SerializedName("status") val status: String,  // ACTIVE, COMPLETED, CANCELLED
    @SerializedName("total_scans") val totalScans: Int = 0,
    @SerializedName("healthy_count") val healthyCount: Int = 0,
    @SerializedName("plague_count") val plagueCount: Int = 0,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("scan_results") val scanResults: List<ScanResultDTO>? = null
)

data class ScanSessionCreateDTO(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("empresa") val empresaId: Int,
    @SerializedName("zona") val zonaId: Int,
    @SerializedName("cultivo") val cultivoId: Int,
    @SerializedName("worker_name") val workerName: String,
    @SerializedName("model_version_string") val modelVersionString: String,
    @SerializedName("started_at") val startedAt: String,
    @SerializedName("status") val status: String = "ACTIVE"
)

data class ScanSessionUpdateDTO(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("status") val status: String? = null,
    @SerializedName("finished_at") val finishedAt: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("total_scans") val totalScans: Int? = null,
    @SerializedName("healthy_count") val healthyCount: Int? = null,
    @SerializedName("plague_count") val plagueCount: Int? = null
)

// ==================== SCAN RESULT DTOs ====================

data class ScanResultDTO(
    @SerializedName("result_id") val resultId: String,
    @SerializedName("session") val sessionId: String,
    @SerializedName("photo_path") val photoPath: String,
    @SerializedName("classification") val classification: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("has_plague") val hasPlague: Boolean,
    @SerializedName("report_id") val reportId: String?,
    @SerializedName("scanned_at") val scannedAt: String,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("bbox") val bbox: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null  // ✅ NUEVO
)

data class ScanResultCreateDTO(
    @SerializedName("result_id") val resultId: String,
    @SerializedName("session") val sessionId: String,
    @SerializedName("photo_path") val photoPath: String,
    @SerializedName("classification") val classification: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("has_plague") val hasPlague: Boolean,
    @SerializedName("report_id") val reportId: Int? = null,
    @SerializedName("scanned_at") val scannedAt: String
)

// ==================== SYNC REQUEST/RESPONSE ====================

data class SessionSyncRequest(
    @SerializedName("sessions") val sessions: List<SessionSyncData>
)

data class SessionSyncData(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("empresa_id") val empresaId: Int,
    @SerializedName("zona_id") val zonaId: Int,
    @SerializedName("cultivo_id") val cultivoId: Int,
    @SerializedName("worker_name") val workerName: String,
    @SerializedName("model_version_string") val modelVersionString: String,
    @SerializedName("started_at") val startedAt: String,
    @SerializedName("finished_at") val finishedAt: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("total_scans") val totalScans: Int,
    @SerializedName("healthy_count") val healthyCount: Int,
    @SerializedName("plague_count") val plagueCount: Int,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("scan_results") val scanResults: List<ScanResultCreateDTO>
)

data class ScanResultSyncRequest(
    @SerializedName("results") val results: List<ScanResultCreateDTO>
)

data class SyncResponse(
    @SerializedName("synced") val synced: List<SyncedItem>,
    @SerializedName("errors") val errors: List<SyncError>,
    @SerializedName("total_synced") val totalSynced: Int,
    @SerializedName("total_errors") val totalErrors: Int
)

data class SyncedItem(
    @SerializedName("session_id") val sessionId: String? = null,
    @SerializedName("result_id") val resultId: String? = null,
    @SerializedName("created") val created: Boolean,
    @SerializedName("scan_results_count") val scanResultsCount: Int? = null
)

data class SyncError(
    @SerializedName("session_id") val sessionId: String? = null,
    @SerializedName("result_id") val resultId: String? = null,
    @SerializedName("error") val error: String
)