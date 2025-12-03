package com.capstone.cropcare.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "reports",
    foreignKeys = [
        ForeignKey(
            entity = ZoneEntity::class,
            parentColumns = ["zoneId"],
            childColumns = ["zoneId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CropEntity::class,
            parentColumns = ["cropId"],
            childColumns = ["cropId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["zoneId"]),
        Index(value = ["cropId"]),
        Index(value = ["workerId"]),
        Index(value = ["sessionId"]),
        Index(value = ["scanResultId"])
    ]
)
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val reportId: Int = 0,
    val workerName: String,
    val workerId: Int,
    val diagnostic: String,
    val zoneId: String,
    val cropId: String,
    val localPhotoPath: String?,
    val observation: String,
    val timestamp: Long = System.currentTimeMillis(),
    val syncedWithBackend: Boolean = false,
    val sessionId: String? = null,
    val scanResultId: String? = null
)