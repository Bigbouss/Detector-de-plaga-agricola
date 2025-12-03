package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.remote.dto.JoinCodeResponse
import com.capstone.cropcare.domain.model.InvitationModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun JoinCodeResponse.toDomain(empresaId: Int): InvitationModel {

    val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    ).onEach { it.timeZone = TimeZone.getTimeZone("UTC") }

    fun parseDate(dateString: String): Long? {
        for (format in dateFormats) {
            try {
                return format.parse(dateString)?.time
            } catch (e: Exception) {
                continue
            }
        }
        android.util.Log.e("InvitationMapper", "No se pudo parsear fecha: $dateString")
        return null
    }

    // Parsear fechas
    val createdAtMillis = parseDate(createdAt) ?: System.currentTimeMillis()
    val expiresAtMillis = expiresAt?.let { parseDate(it) }
        ?: (createdAtMillis + 7 * 24 * 60 * 60 * 1000L)

    // Determinar estado del código
    val isUsed = usedCount >= maxUses
    val isExpired = expiresAtMillis < System.currentTimeMillis()
    val isActive = !isUsed && !isExpired && !revoked

    return InvitationModel(
        id = id.toString(),
        code = code,
        organizationId = empresa.toString(),
        organizationName = "Empresa #$empresa",
        createdBy = "",
        usedBy = usedByEmail,
        isActive = isActive,
        isUsed = isUsed,
        expiresAt = expiresAtMillis,
        createdAt = createdAtMillis
    )
}