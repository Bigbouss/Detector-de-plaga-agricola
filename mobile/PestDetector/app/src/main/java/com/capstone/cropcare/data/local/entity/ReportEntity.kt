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
        Index(value = ["cropId"])
    ]
)
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val reportId: Int = 0,
    val workerName: String,
    val diagnostic: String,
    val zoneId: String,
    val cropId: String,
    val localPhotoPath: String?,
    val observation: String,
    val timestamp: Long = System.currentTimeMillis(),
    val syncedWithBackend: Boolean = false
)


//@Entity(tableName = "report_form")
//data class ReportEntity(
//    @PrimaryKey(autoGenerate = true) val id: Int = 0,
//    @ColumnInfo(name = "worker_name") val workerName: String,
//    @ColumnInfo(name = "diagnostic") val diagnostic: String,
//    @ColumnInfo(name = "crop_zone") val cropZone: String,
//    @ColumnInfo(name = "local_photo_path") val localPhotoPath: String? = null,
//    @ColumnInfo(name = "remote_photo_url") val remotePhotoUrl: String? = null,
//    @ColumnInfo(name = "observation") val observation: String,
//    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
//    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
//)
