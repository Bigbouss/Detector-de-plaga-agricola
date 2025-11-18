//package com.capstone.cropcare.domain.model
//
//data class ScanResultModel(
//    val id: String = java.util.UUID.randomUUID().toString(),
//    val sessionId: String, // FK a ScanSession
//    val photoPath: String,
//    val classification: String, // "healthy" o el tipo de plaga
//    val confidence: Float, // 0.0 - 1.0
//    val hasPlague: Boolean, // true si es plaga, false si es sana
//    val scannedAt: Long = System.currentTimeMillis(),
//
//    // Si hay plaga, se puede crear un reporte
//    val reportId: String? = null, // FK a Report (opcional)
//
//    val syncedWithBackend: Boolean = false
//)