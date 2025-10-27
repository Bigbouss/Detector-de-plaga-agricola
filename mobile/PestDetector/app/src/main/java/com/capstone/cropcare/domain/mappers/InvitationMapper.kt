package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.remote.dto.JoinCodeResponse
import com.capstone.cropcare.domain.model.InvitationModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun JoinCodeResponse.toDomain(organizationName: String = ""): InvitationModel {
    // ... código existente de parsing de fechas ...

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
        android.util.Log.e("InvitationMapper", "❌ No se pudo parsear fecha: $dateString")
        return null
    }

    val createdAtMillis = parseDate(createdAt) ?: System.currentTimeMillis()
    val expiresAtMillis = expiresAt?.let { parseDate(it) } ?: (createdAtMillis + 7 * 24 * 60 * 60 * 1000L)

    val isUsed = usedCount >= maxUses
    val isExpired = expiresAtMillis < System.currentTimeMillis()

    return InvitationModel(
        id = id.toString(),
        code = code,
        organizationId = empresa.toString(),
        organizationName = organizationName,
        createdBy = "",
        usedBy = usedByEmail,  // ← CAMBIO: usar usedByEmail del DTO
        isActive = !isUsed && !isExpired && !revoked,
        isUsed = isUsed,
        expiresAt = expiresAtMillis,
        createdAt = createdAtMillis
    )
}