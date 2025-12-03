package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.remote.dto.*
import com.capstone.cropcare.domain.model.ScanResultModel
import com.capstone.cropcare.domain.model.ScanSessionModel
import com.capstone.cropcare.domain.model.SessionStatus
import java.text.SimpleDateFormat
import java.util.*

// ==================== DATE UTILITIES ====================

private val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun Long.toISO8601(): String {
    return iso8601Format.format(Date(this))
}

fun String.fromISO8601(): Long {
    return try {
        iso8601Format.parse(this)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

// ==================== SESSION MAPPERS ====================

suspend fun ScanSessionModel.toCreateDTO(empresaId: Int): ScanSessionCreateDTO {
    return ScanSessionCreateDTO(
        sessionId = id,
        empresaId = empresaId,
        zonaId = zoneId.toIntOrNull() ?: 0,
        cultivoId = cropId.toIntOrNull() ?: 0,
        workerName = workerName,
        modelVersionString = modelVersion,
        startedAt = startedAt.toISO8601(),
        status = status.name
    )
}

fun ScanSessionModel.toUpdateDTO(): ScanSessionUpdateDTO {
    return ScanSessionUpdateDTO(
        sessionId = id,
        status = status.name,
        finishedAt = finishedAt?.toISO8601(),
        notes = notes,
        totalScans = totalScans,
        healthyCount = healthyCount,
        plagueCount = plagueCount
    )
}

suspend fun ScanSessionModel.toSyncData(scanResults: List<ScanResultModel>, empresaId: Int): SessionSyncData {
    return SessionSyncData(
        sessionId = id,
        empresaId = empresaId,
        zonaId = zoneId.toIntOrNull() ?: 0,
        cultivoId = cropId.toIntOrNull() ?: 0,
        workerName = workerName,
        modelVersionString = modelVersion,
        startedAt = startedAt.toISO8601(),
        finishedAt = finishedAt?.toISO8601(),
        status = status.name,
        totalScans = totalScans,
        healthyCount = healthyCount,
        plagueCount = plagueCount,
        notes = notes,
        scanResults = scanResults.map { it.toCreateDTO() }
    )
}

fun ScanSessionDTO.toDomain(): ScanSessionModel {
    return ScanSessionModel(
        id = sessionId,
        workerId = ownerId.toString(),
        workerName = workerName,
        zoneId = zonaId.toString(),
        zoneName = zonaName ?: "",
        cropId = cultivoId.toString(),
        cropName = cultivoName ?: "",
        startedAt = startedAt.fromISO8601(),
        finishedAt = finishedAt?.fromISO8601(),
        status = SessionStatus.valueOf(status),
        totalScans = totalScans,
        healthyCount = healthyCount,
        plagueCount = plagueCount,
        modelVersion = modelVersionString,
        notes = notes,
        syncedWithBackend = true
    )
}

// ==================== SCAN RESULT MAPPERS ====================

fun ScanResultModel.toCreateDTO(): ScanResultCreateDTO {
    return ScanResultCreateDTO(
        resultId = id,
        sessionId = sessionId,
        photoPath = photoPath,
        classification = classification,
        confidence = confidence,
        hasPlague = hasPlague,
        reportId = reportId?.toIntOrNull(),
        scannedAt = scannedAt.toISO8601()
    )
}

fun ScanResultDTO.toDomain(): ScanResultModel {
    return ScanResultModel(
        id = resultId,
        sessionId = sessionId,
        photoPath = photoPath,
        classification = classification,
        confidence = confidence,
        hasPlague = hasPlague,
        scannedAt = scannedAt.fromISO8601(),
        reportId = reportId?.toString(),
        syncedWithBackend = true
    )
}

// ==================== BATCH MAPPERS ====================

suspend fun List<ScanSessionModel>.toSyncRequest(
    scanResultsMap: Map<String, List<ScanResultModel>>,
    empresaId: Int
): SessionSyncRequest {
    return SessionSyncRequest(
        sessions = this.map { session ->
            val results = scanResultsMap[session.id] ?: emptyList()
            session.toSyncData(results, empresaId)
        }
    )
}

fun List<ScanResultModel>.toSyncRequest(): ScanResultSyncRequest {
    return ScanResultSyncRequest(
        results = this.map { it.toCreateDTO() }
    )
}