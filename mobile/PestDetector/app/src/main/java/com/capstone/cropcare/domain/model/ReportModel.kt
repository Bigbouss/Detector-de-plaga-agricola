package com.capstone.cropcare.domain.model


data class ReportModel(
    val id: Int = 0,
    val workerName: String,
    val diagnostic: String,
    val zone: ZoneModel,
    val crop: CropModel,
    val photoPath: String?,
    val observation: String,
    val timestamp: Long,
    val syncedWithBackend: Boolean = false
)

//data class ReportModel(
//    val id: Int = 0,
//
//    // Información del worker (se puede omitir, viene de la sesión)
//    val workerName: String,
//
//    // Información del diagnóstico
//    val diagnostic: String,
//    val confidence: Float? = null, // Del modelo ML
//
//    // Información de zona/cultivo (viene de la sesión)
//    val zone: ZoneModel,
//    val crop: CropModel,
//
//    // Foto y observaciones
//    val photoPath: String?,
//    val observation: String,
//    val timestamp: Long,
//
//    // 🆕 Vinculación a sesión de escaneo
//    val sessionId: String? = null, // Si es parte de una sesión
//    val scanResultId: String? = null, // Si es de un escaneo específico
//
//    val syncedWithBackend: Boolean = false
//)