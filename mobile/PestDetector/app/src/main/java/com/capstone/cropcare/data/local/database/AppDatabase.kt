package com.capstone.cropcare.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.capstone.cropcare.data.local.dao.ReportDao
import com.capstone.cropcare.data.local.entity.ReportEntity

@Database(entities = [ReportEntity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract  fun reportDao(): ReportDao
}