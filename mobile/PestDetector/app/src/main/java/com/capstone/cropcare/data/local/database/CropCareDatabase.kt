package com.capstone.cropcare.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.capstone.cropcare.data.local.dao.CropDao
import com.capstone.cropcare.data.local.dao.ReportDao
import com.capstone.cropcare.data.local.dao.WorkerZoneAssignmentDao
import com.capstone.cropcare.data.local.dao.ZoneDao
import com.capstone.cropcare.data.local.entity.CropEntity
import com.capstone.cropcare.data.local.entity.ReportEntity
import com.capstone.cropcare.data.local.entity.WorkerZoneAssignmentEntity
import com.capstone.cropcare.data.local.entity.ZoneEntity

@Database(
    entities = [
        ZoneEntity::class,
        CropEntity::class,
        ReportEntity::class,
        WorkerZoneAssignmentEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CropCareDatabase : RoomDatabase() {
    abstract fun zoneDao(): ZoneDao
    abstract fun cropDao(): CropDao
    abstract fun reportDao(): ReportDao
    abstract fun workerZoneAssignmentDao(): WorkerZoneAssignmentDao
}