package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.remote.dto.JoinCodeResponse
import com.capstone.cropcare.domain.model.InvitationModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun JoinCodeResponse.toDomain(organizationName: String = ""): InvitationModel {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    val createdAtMillis = try {
        dateFormat.parse(createdAt)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    val expiresAtMillis = try {
        expiresAt?.let { dateFormat.parse(it)?.time } ?: (createdAtMillis + 7 * 24 * 60 * 60 * 1000L)
    } catch (e: Exception) {
        createdAtMillis + 7 * 24 * 60 * 60 * 1000L
    }

    val isUsed = usedCount >= maxUses
    val isExpired = expiresAtMillis < System.currentTimeMillis()

    return InvitationModel(
        id = id.toString(),
        code = code,
        organizationId = empresa.toString(),
        organizationName = organizationName, // Lo pasamos como parámetro
        createdBy = "", // No lo devuelve el backend
        usedBy = null,
        isActive = !isUsed && !isExpired && !revoked,
        isUsed = isUsed,
        expiresAt = expiresAtMillis,
        createdAt = createdAtMillis
    )
}