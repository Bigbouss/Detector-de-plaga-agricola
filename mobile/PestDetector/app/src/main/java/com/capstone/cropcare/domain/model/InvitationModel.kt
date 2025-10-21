package com.capstone.cropcare.domain.model

data class InvitationModel(
    val id: String,
    val code: String,
    val organizationId: String,
    val organizationName: String,
    val createdBy: String,
    val usedBy: String? = null,
    val isActive: Boolean = true,
    val isUsed: Boolean = false,
    val expiresAt: Long,
    val createdAt: Long = System.currentTimeMillis()
)