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
data class AssignZonesResponse(
    val message: String,
    @SerializedName("worker_id")
    val workerId: Int,
    @SerializedName("assigned_zone_ids")
    val assignedZoneIds: List<Int>
)

data class WorkerZonesResponse(
    @SerializedName("worker_id")
    val workerId: Int,
    @SerializedName("zone_ids")
    val zoneIds: List<Int>
)