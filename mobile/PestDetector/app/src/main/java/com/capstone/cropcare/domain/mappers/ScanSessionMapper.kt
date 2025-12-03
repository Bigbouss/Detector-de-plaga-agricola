package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.local.entity.ScanSessionEntity
import com.capstone.cropcare.domain.model.ScanSessionModel
import com.capstone.cropcare.domain.model.SessionStatus

fun ScanSessionEntity.toDomain() = ScanSessionModel(
    id = sessionId,
    workerId = workerId,
    workerName = workerName,
    zoneId = zoneId,
    zoneName = zoneName,
    cropId = cropId,
    cropName = cropName,
    startedAt = startedAt,
    finishedAt = finishedAt,
    status = SessionStatus.valueOf(status),
    totalScans = totalScans,
    healthyCount = healthyCount,
    plagueCount = plagueCount,
    modelVersion = modelVersion,
    notes = notes,
    syncedWithBackend = syncedWithBackend
)

fun ScanSessionModel.toEntity() = ScanSessionEntity(
    sessionId = id,
    workerId = workerId,
    workerName = workerName,
    zoneId = zoneId,
    zoneName = zoneName,
    cropId = cropId,
    cropName = cropName,
    startedAt = startedAt,
    finishedAt = finishedAt,
    status = status.name,
    totalScans = totalScans,
    healthyCount = healthyCount,
    plagueCount = plagueCount,
    modelVersion = modelVersion,
    notes = notes,
    syncedWithBackend = syncedWithBackend
)