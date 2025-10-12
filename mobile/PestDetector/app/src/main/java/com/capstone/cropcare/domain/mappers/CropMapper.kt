package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.local.entity.CropEntity
import com.capstone.cropcare.domain.model.CropModel

fun CropEntity.toDomain() = CropModel(
    id = cropId,
    name = cropName,
    zoneId = zoneId
)

fun CropModel.toEntity() = CropEntity(
    cropId = id,
    cropName = name,
    zoneId = zoneId
)