package com.capstone.cropcare.data.remote.dto

import com.google.gson.annotations.SerializedName

// ========== REQUESTS ==========
data class AssignZonesRequest(
    @SerializedName("worker_id")
    val workerId: Int,
    @SerializedName("zone_ids")
    val zoneIds: List<Int>
)

// ========== RESPONSES ==========
data class WorkerZoneAssignmentResponse(
    val id: Int,
    val worker: Int,
    val zones: List<ZoneResponse>,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class AssignedZoneDto(
    val id: Int,
    val nombre: String,
    val descripcion: String?
)