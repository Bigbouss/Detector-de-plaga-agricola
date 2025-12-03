package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.local.entity.ReportEntity
import com.capstone.cropcare.data.local.entity.relations.ReportWithDetails
import com.capstone.cropcare.domain.model.ReportModel
import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.model.ZoneModel

// Mapper para ReportEntity
fun ReportEntity.toDomain() = ReportModel(
    id = reportId,
    workerName = workerName,
    workerId = workerId,
    diagnostic = diagnostic,
    confidence = null,
    zone = ZoneModel(
        id = zoneId,
        name = "",
        description = null
    ),
    crop = CropModel(
        id = cropId,
        name = "",
        zoneId = zoneId
    ),
    photoPath = localPhotoPath,
    observation = observation,
    timestamp = timestamp,
    sessionId = sessionId,
    scanResultId = scanResultId,
    syncedWithBackend = syncedWithBackend
)

// Mapper para ReportWithDetails
fun ReportWithDetails.toDomain(): ReportModel? {
    if (zone == null || crop == null) return null

    return ReportModel(
        id = report.reportId,
        workerName = report.workerName,
        workerId = report.workerId,
        diagnostic = report.diagnostic,
        confidence = null,
        zone = zone.toDomain(),
        crop = crop.toDomain(),
        photoPath = report.localPhotoPath,
        observation = report.observation,
        timestamp = report.timestamp,
        sessionId = report.sessionId,
        scanResultId = report.scanResultId,
        syncedWithBackend = report.syncedWithBackend
    )
}

// Domain → Entity
fun ReportModel.toEntity() = ReportEntity(
    reportId = id,
    workerName = workerName,
    workerId = workerId,
    diagnostic = diagnostic,
    zoneId = zone.id,
    cropId = crop.id,
    localPhotoPath = photoPath,
    observation = observation,
    timestamp = timestamp,
    syncedWithBackend = syncedWithBackend,
    sessionId = sessionId,
    scanResultId = scanResultId
)