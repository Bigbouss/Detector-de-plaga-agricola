package com.capstone.cropcare.domain.model


data class ReportModel(
    val id: Int = 0,
    val workerName: String = "",
    val diagnostic: String = "",
    val cropZone: String = "",
    val localPhotoPath: String? = null,
    val observation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
