package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.local.entity.ReportEntity
import com.capstone.cropcare.domain.model.ReportModel

fun ReportModel.toEntity(): ReportEntity {
    return ReportEntity(
        id = this.id,
        workerName = this.workerName,
        diagnostic = this.diagnostic,
        cropZone = this.cropZone,
        localPhotoPath = this.localPhotoPath,
        remotePhotoUrl = null,
        observation = this.observation,
        timestamp = this.timestamp,
        isSynced = false
    )
}

fun ReportEntity.toModel(): ReportModel {
    return ReportModel(
        id = this.id,
        workerName = this.workerName,
        diagnostic = this.diagnostic,
        cropZone = this.cropZone,
        localPhotoPath = this.localPhotoPath,
        observation = this.observation,
        timestamp = this.timestamp
    )
}
