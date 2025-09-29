package com.capstone.cropcare.domain.entity


data class ReportHistory(
    val issueType: String,
    val issueName: String,
    val zoneName: String,
    val cropName: String,
    val date: String,
    val hour: String
)
