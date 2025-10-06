package com.capstone.cropcare.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "report_form")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "worker_name") val workerName: String,
    @ColumnInfo(name = "diagnostic") val diagnostic: String,
    @ColumnInfo(name = "crop_zone") val cropZone: String,
    @ColumnInfo(name = "local_photo_path") val localPhotoPath: String? = null,
    @ColumnInfo(name = "remote_photo_url") val remotePhotoUrl: String? = null,
    @ColumnInfo(name = "observation") val observation: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
)
