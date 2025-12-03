package com.capstone.cropcare.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== REQUESTS ====================

data class LoginRequest(
    val email: String,  // ✅ Backend Django espera "email"
    val password: String
)

data class RegisterAdminRequest(
    val email: String,
    val password: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val phone: String = "",
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("tax_id")
    val taxId: String
)

data class RegisterWorkerRequest(
    val email: String,
    val password: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val phone: String = "",
    @SerializedName("join_code")
    val joinCode: String
)

data class ValidateCodeRequest(
    val code: String
)

data class RefreshTokenRequest(
    val refresh: String
)

// ==================== RESPONSES ====================

data class TokenResponse(
    val access: String,
    val refresh: String
)

data class RefreshTokenResponse(
    val access: String
)

data class RegisterAdminResponse(
    val message: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("empresa_id")
    val empresaId: Int
)

data class RegisterWorkerResponse(
    val message: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("empresa_id")
    val empresaId: Int
)

data class ValidateCodeResponse(
    val valid: Boolean,
    val empresa: String? = null,        // ✅ Cambio: era "companyName"
    @SerializedName("expires_at")
    val expiresAt: String? = null,
    @SerializedName("max_uses")
    val maxUses: Int? = null,
    @SerializedName("used_count")
    val usedCount: Int? = null,
    val error: String? = null
)

data class ErrorResponse(
    val error: String? = null,
    val detail: String? = null,
    val message: String? = null
)