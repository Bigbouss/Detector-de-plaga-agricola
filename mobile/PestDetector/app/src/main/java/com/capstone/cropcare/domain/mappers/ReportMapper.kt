package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.local.entity.ReportEntity
import com.capstone.cropcare.data.local.entity.relations.ReportWithDetails
import com.capstone.cropcare.domain.model.ReportModel

// Mapper para ReportEntity (sin detalles)
fun ReportEntity.toDomain() = ReportModel(
    id = reportId,
    workerName = workerName,
    diagnostic = diagnostic,
    zone = com.capstone.cropcare.domain.model.ZoneModel(
        id = zoneId,
        name = "", // No tenemos el nombre aquí
        description = null
    ),
    crop = com.capstone.cropcare.domain.model.CropModel(
        id = cropId,
        name = "", // No tenemos el nombre aquí
        zoneId = zoneId
    ),
    photoPath = localPhotoPath,
    observation = observation,
    timestamp = timestamp,
    syncedWithBackend = syncedWithBackend
)

// Mapper para ReportWithDetails (con Zone y Crop completos)
fun ReportWithDetails.toDomain(): ReportModel? {
    // Si no tiene zone o crop, retorna null
    if (zone == null || crop == null) return null

    return ReportModel(
        id = report.reportId,
        workerName = report.workerName,
        diagnostic = report.diagnostic,
        zone = zone.toDomain(),
        crop = crop.toDomain(),
        photoPath = report.localPhotoPath,
        observation = report.observation,
        timestamp = report.timestamp,
        syncedWithBackend = report.syncedWithBackend
    )
}

// Mapper de Domain a Entity
fun ReportModel.toEntity() = ReportEntity(
    reportId = id,
    workerName = workerName,
    diagnostic = diagnostic,
    zoneId = zone.id,
    cropId = crop.id,
    localPhotoPath = photoPath,
    observation = observation,
    timestamp = timestamp,
    syncedWithBackend = syncedWithBackend
)