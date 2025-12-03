package com.capstone.cropcare.domain.model

data class ScanResultModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sessionId: String,
    val photoPath: String,
    val classification: String,
    val confidence: Float,
    val hasPlague: Boolean,
    val scannedAt: Long = System.currentTimeMillis(),

    // Si hay plaga, se puede crear un reporte
    val reportId: String? = null,

    val syncedWithBackend: Boolean = false
)