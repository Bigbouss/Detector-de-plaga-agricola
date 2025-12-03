package com.capstone.cropcare.domain.mappers

import com.capstone.cropcare.data.remote.dto.WorkerResponse
import com.capstone.cropcare.domain.model.WorkerModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun WorkerResponse.toDomain(): WorkerModel {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    val joinedAtMillis = try {
        dateFormat.parse(profile.joinedAt)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    val fullName = buildString {
        if (user.firstName.isNotBlank()) append(user.firstName)
        if (user.lastName.isNotBlank()) {
            if (isNotEmpty()) append(" ")
            append(user.lastName)
        }
        if (isEmpty()) append(user.username)
    }

    return WorkerModel(
        id = user.id,
        name = fullName,
        email = user.email,
        phoneNumber = null,
        canManagePlots = profile.canManagePlots,
        isActive = profile.isActive,
        joinedAt = joinedAtMillis,
        assignedZoneIds = emptyList()
    )
}