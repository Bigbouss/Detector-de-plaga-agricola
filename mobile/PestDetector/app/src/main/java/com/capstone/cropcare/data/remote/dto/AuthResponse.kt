package com.capstone.cropcare.data.remote.dto

import com.google.gson.annotations.SerializedName

// ========== REQUESTS ==========
data class LoginRequest(
    val username: String, // Backend usa username (que es el email)
    val password: String
)

data class RegisterAdminRequest(
    val email: String,
    val password: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("company_name")
    val companyName: String
)

data class RegisterWorkerRequest(
    val email: String,
    val password: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("join_code")
    val joinCode: String
)

data class ValidateCodeRequest(
    val code: String
)

data class RefreshTokenRequest(
    val refresh: String
)

// ========== RESPONSES ==========

// Respuesta de Login (Djoser devuelve solo tokens)
data class TokenResponse(
    val access: String,
    val refresh: String
)

// Respuesta de Register (Admin y Worker)
data class AuthResponse(
    val user: BackendUserDto,
    val profile: ProfileDto,
    val empresa: EmpresaDto,
    val tokens: TokensDto
)

data class BackendUserDto(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
)

data class ProfileDto(
    val user: ProfileUserDto,
    val role: String, // "ADMIN" o "WORKER"
    val empresa: EmpresaDto,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("can_manage_plots")
    val canManagePlots: Boolean,
    @SerializedName("joined_at")
    val joinedAt: String
)

data class ProfileUserDto(
    val id: Int,
    val username: String
)

data class EmpresaDto(
    val id: Int,
    val name: String,
    @SerializedName("legal_name")
    val legalName: String?,
    @SerializedName("tax_id")
    val taxId: String?,
    val country: String,
    val timezone: String,
    val owner: Int,
    @SerializedName("created_at")
    val createdAt: String
)

data class TokensDto(
    val access: String,
    val refresh: String
)

data class ValidateCodeResponse(
    val valid: Boolean,
    @SerializedName("company_name")
    val companyName: String? = null,
    @SerializedName("empresa_id")
    val empresaId: Int? = null,
    val error: String? = null
)

data class RefreshTokenResponse(
    val access: String
)

// Respuesta del endpoint /me
data class MeResponse(
    val user: BackendUserDto,
    val profile: ProfileDto,
    val empresa: EmpresaDto?
)

// ========== ERROR RESPONSE ==========
data class ErrorResponse(
    val error: String? = null,
    val detail: String? = null,
    val message: String? = null
)