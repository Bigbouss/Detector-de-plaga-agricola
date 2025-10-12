package com.capstone.cropcare.data.local.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.capstone.cropcare.data.local.entity.CropEntity
import com.capstone.cropcare.data.local.entity.ReportEntity
import com.capstone.cropcare.data.local.entity.ZoneEntity

data class ReportWithDetails(
    @Embedded val report: ReportEntity,
    @Relation(
        parentColumn = "zoneId",
        entityColumn = "zoneId"
    )
    val zone: ZoneEntity?,
    @Relation(
        parentColumn = "cropId",
        entityColumn = "cropId"
    )
    val crop: CropEntity?
)