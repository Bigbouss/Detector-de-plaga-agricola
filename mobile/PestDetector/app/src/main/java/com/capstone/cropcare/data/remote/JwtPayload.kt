package com.capstone.cropcare.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Payload del JWT del nuevo backend Django
 * Incluye: user_id, email, role, empresa_id
 */
data class JwtPayload(
    @SerializedName("user_id")
    val userId: Int?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("role")
    val role: String?,  // "ADMIN" o "WORKER"

    @SerializedName("empresa_id")
    val empresaId: Int?,

    // Claims estándar JWT
    @SerializedName("token_type")
    val tokenType: String?,

    @SerializedName("exp")
    val exp: Long?,

    @SerializedName("iat")
    val iat: Long?,

    @SerializedName("jti")
    val jti: String?
)