package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.local.entity.ZoneEntity
import com.capstone.cropcare.domain.model.ZoneModel

fun ZoneEntity.toDomain() = ZoneModel(
    id = zoneId,
    name = zoneName,
    description = description
)

fun ZoneModel.toEntity() = ZoneEntity(
    zoneId = id,
    zoneName = name,
    description = description
)