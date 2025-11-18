//package com.capstone.cropcare.domain.model
//
//data class ScanSessionModel(
//    val id: String = java.util.UUID.randomUUID().toString(),
//    val workerId: String,
//    val workerName: String,
//    val zoneId: String,
//    val zoneName: String,
//    val cropId: String,
//    val cropName: String,
//    val startedAt: Long = System.currentTimeMillis(),
//    val finishedAt: Long? = null,
//    val status: SessionStatus = SessionStatus.ACTIVE,
//
//    // Métricas de la sesión
//    val totalScans: Int = 0,
//    val healthyCount: Int = 0,
//    val plagueCount: Int = 0,
//
//    // Metadatos
//    val modelVersion: String = "1.0", // Versión del modelo ML usado
//    val notes: String? = null,
//    val syncedWithBackend: Boolean = false
//)
//
//enum class SessionStatus {
//    ACTIVE,     // Sesión en curso
//    COMPLETED,  // Sesión finalizada
//    CANCELLED   // Sesión cancelada
//}
