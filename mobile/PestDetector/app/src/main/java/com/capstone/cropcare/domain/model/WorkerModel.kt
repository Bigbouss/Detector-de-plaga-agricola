package com.capstone.cropcare.domain.model

data class WorkerModel(
    val id: Int,
    val name: String,
    val email: String,
    val phoneNumber: String? = null,
    val canManagePlots: Boolean = false,
    val isActive: Boolean = true,
    val joinedAt: Long = System.currentTimeMillis(),
    val assignedZoneIds: List<String> = emptyList()
)