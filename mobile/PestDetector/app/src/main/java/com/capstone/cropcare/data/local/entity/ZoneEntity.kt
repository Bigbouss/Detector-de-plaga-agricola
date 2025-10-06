package com.capstone.cropcare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "zones")
data class ZoneEntity(
    @PrimaryKey val zoneId: String = UUID.randomUUID().toString(),
    val zoneName: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)