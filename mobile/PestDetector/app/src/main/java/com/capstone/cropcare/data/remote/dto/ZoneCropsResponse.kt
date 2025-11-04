package com.capstone.cropcare.data.remote.dto

import com.google.gson.annotations.SerializedName

// ========== REQUESTS ==========
data class CreateZoneRequest(
    val nombre: String,
    val descripcion: String? = null
)

data class CreateCropRequest(
    val nombre: String,
    val zona: Int  // ID de la zona
)

// ========== RESPONSES ==========
data class ZoneResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val empresa: Int,
    val cultivos: List<CropDto>,
    @SerializedName("cultivos_count")
    val cultivosCount: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class CropDto(
    val id: Int,
    val nombre: String,
    val zona: Int,
    @SerializedName("created_at")
    val createdAt: String
)