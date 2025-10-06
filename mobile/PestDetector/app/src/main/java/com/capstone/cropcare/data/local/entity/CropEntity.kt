package com.capstone.cropcare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "crops",
    foreignKeys = [
        ForeignKey(
            entity = ZoneEntity::class,
            parentColumns = ["zoneId"],
            childColumns = ["zoneId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CropEntity(
    @PrimaryKey val cropId: String = UUID.randomUUID().toString(),
    val cropName: String,
    val zoneId: String,
    val createdAt: Long = System.currentTimeMillis()
)