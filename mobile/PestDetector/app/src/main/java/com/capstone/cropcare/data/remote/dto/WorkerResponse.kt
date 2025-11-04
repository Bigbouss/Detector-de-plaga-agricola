package com.capstone.cropcare.data.remote.dto

import com.google.gson.annotations.SerializedName

// Respuesta de lista de workers
data class WorkerResponse(
    val id: Int,
    val user: WorkerUserDto,
    val profile: WorkerProfileDto
)

data class WorkerUserDto(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
)

data class WorkerProfileDto(
    val role: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("can_manage_plots")
    val canManagePlots: Boolean,
    @SerializedName("joined_at")
    val joinedAt: String
)