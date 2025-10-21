package com.capstone.cropcare.data.remote.dto

// ========== REQUESTS ==========
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterAdminRequest(
    val email: String,
    val password: String,
    val name: String,
    val organization_name: String
)

data class RegisterWorkerRequest(
    val email: String,
    val password: String,
    val name: String,
    val invitation_code: String
)

data class ValidateCodeRequest(
    val code: String
)

data class RefreshTokenRequest(
    val refresh: String
)

// ========== RESPONSES ==========
data class AuthResponse(
    val user: UserDto,
    val tokens: TokensDto
)

data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val role: String, // "ADMIN" o "WORKER"
    val organization_id: String,
    val organization_name: String,
    val must_change_password: Boolean = false
)

data class TokensDto(
    val access: String,
    val refresh: String
)

data class ValidateCodeResponse(
    val valid: Boolean,
    val organization_name: String? = null,
    val error: String? = null
)

data class RefreshTokenResponse(
    val access: String
)

// ========== ERROR RESPONSE ==========
data class ErrorResponse(
    val error: String?,
    val detail: String?,
    val message: String?
)