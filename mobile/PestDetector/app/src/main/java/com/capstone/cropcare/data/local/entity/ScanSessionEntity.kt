package com.capstone.cropcare.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_sessions")
data class ScanSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val workerId: String,
    val workerName: String,
    val zoneId: String,
    val zoneName: String,
    val cropId: String,
    val cropName: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val status: String, // ACTIVE, COMPLETED, CANCELLED

    // Métricas
    val totalScans: Int,
    val healthyCount: Int,
    val plagueCount: Int,

    // Metadatos
    val modelVersion: String,
    val notes: String?,
    val syncedWithBackend: Boolean
)