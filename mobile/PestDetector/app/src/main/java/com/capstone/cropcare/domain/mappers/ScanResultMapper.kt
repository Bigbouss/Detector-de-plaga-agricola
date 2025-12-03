package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.local.entity.ScanResultEntity
import com.capstone.cropcare.domain.model.ScanResultModel

fun ScanResultEntity.toDomain() = ScanResultModel(
    id = scanId,
    sessionId = sessionId,
    photoPath = photoPath,
    classification = classification,
    confidence = confidence,
    hasPlague = hasPlague,
    scannedAt = scannedAt,
    reportId = reportId,
    syncedWithBackend = syncedWithBackend
)

fun ScanResultModel.toEntity() = ScanResultEntity(
    scanId = id,
    sessionId = sessionId,
    photoPath = photoPath,
    classification = classification,
    confidence = confidence,
    hasPlague = hasPlague,
    scannedAt = scannedAt,
    reportId = reportId,
    syncedWithBackend = syncedWithBackend
)