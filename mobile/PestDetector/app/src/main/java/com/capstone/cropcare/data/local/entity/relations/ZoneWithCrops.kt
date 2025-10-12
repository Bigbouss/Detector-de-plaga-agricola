package com.capstone.cropcare.data.local.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.capstone.cropcare.data.local.entity.CropEntity
import com.capstone.cropcare.data.local.entity.ZoneEntity

data class ZoneWithCrops(
    @Embedded val zone: ZoneEntity,
    @Relation(
        parentColumn = "zoneId",
        entityColumn = "zoneId"
    )
    val crops: List<CropEntity>
)