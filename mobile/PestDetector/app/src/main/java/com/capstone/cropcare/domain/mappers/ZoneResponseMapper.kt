package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.remote.dto.CropDto
import com.capstone.cropcare.data.remote.dto.ZoneResponse
import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.model.ZoneModel

fun ZoneResponse.toDomainZone(): ZoneModel {
    return ZoneModel(
        id = id.toString(),
        name = nombre,
        description = descripcion
    )
}

fun ZoneResponse.toDomainCrops(): List<CropModel> {
    return cultivos.map { it.toDomain() }
}

fun CropDto.toDomain(): CropModel {
    return CropModel(
        id = id.toString(),
        name = nombre,
        zoneId = zona.toString()
    )
}