package com.capstone.cropcare.domain.model


data class ReportModel(
    val id: Int = 0,

    // Información del worker
    val workerName: String,
    val workerId: Int,

    // Diagnóstico
    val diagnostic: String,
    val confidence: Float? = null,

    // Zona / cultivo
    val zone: ZoneModel,
    val crop: CropModel,

    // Foto y observación
    val photoPath: String?,
    val observation: String,
    val timestamp: Long,

    //vínculo con sesión y escaneo
    val sessionId: String? = null,
    val scanResultId: String? = null,

    val syncedWithBackend: Boolean = false
)

