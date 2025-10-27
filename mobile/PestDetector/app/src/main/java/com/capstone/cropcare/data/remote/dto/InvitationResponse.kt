package com.capstone.cropcare.data.remote.dto

import com.google.gson.annotations.SerializedName

// ========== REQUESTS ==========
data class CreateJoinCodeRequest(
    val role: String = "WORKER",
    @SerializedName("max_uses")
    val maxUses: Int = 1,
    @SerializedName("expires_at")
    val expiresAt: String // ISO 8601 format
)

// ========== RESPONSES ==========
data class JoinCodeResponse(
    val id: Int,
    val code: String,
    val empresa: Int,
    val role: String,
    @SerializedName("max_uses")
    val maxUses: Int,
    @SerializedName("used_count")
    val usedCount: Int,
    @SerializedName("expires_at")
    val expiresAt: String?,
    val revoked: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("used_by_email")  // ← NUEVO
    val usedByEmail: String? = null
)

data class JoinCodeListResponse(
    val results: List<JoinCodeResponse>
)