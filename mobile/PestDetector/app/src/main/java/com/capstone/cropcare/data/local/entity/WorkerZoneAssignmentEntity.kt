package com.capstone.cropcare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "worker_zone_assignments")
data class WorkerZoneAssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workerId: String,
    val zoneId: String,
    val assignedAt: Long = System.currentTimeMillis()
)