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