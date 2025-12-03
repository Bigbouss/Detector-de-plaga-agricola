package com.capstone.cropcare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "scan_results",
    foreignKeys = [
        ForeignKey(
            entity = ScanSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class ScanResultEntity(
    @PrimaryKey
    val scanId: String,
    val sessionId: String,
    val photoPath: String,
    val classification: String,
    val confidence: Float,
    val hasPlague: Boolean,
    val scannedAt: Long,
    val reportId: String?,
    val syncedWithBackend: Boolean
)