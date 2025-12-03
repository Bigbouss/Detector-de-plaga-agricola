package com.capstone.cropcare.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.capstone.cropcare.data.local.dao.*
import com.capstone.cropcare.data.local.entity.*

@Database(
    entities = [
        ZoneEntity::class,
        CropEntity::class,
        ReportEntity::class,
        WorkerZoneAssignmentEntity::class,
        ScanSessionEntity::class,
        ScanResultEntity::class
    ],
    version = 7, // Esta version incrementa cada vez que se desea migrar a una nueva version (cambios en entitys)
    exportSchema = false
)
abstract class CropCareDatabase : RoomDatabase() {

    // --- DAOs Existentes ---
    abstract fun zoneDao(): ZoneDao
    abstract fun cropDao(): CropDao
    abstract fun reportDao(): ReportDao
    abstract fun workerZoneAssignmentDao(): WorkerZoneAssignmentDao

    // --- DAOs para Scan ---
    abstract fun scanSessionDao(): ScanSessionDao
    abstract fun scanResultDao(): ScanResultDao

    companion object {
        private const val DATABASE_NAME = "cropcare_database"

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columna workerId con valor por defecto 0
                database.execSQL(
                    "ALTER TABLE reports ADD COLUMN workerId INTEGER NOT NULL DEFAULT 0"
                )

                // Crear índice para workerId
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_reports_workerId ON reports(workerId)"
                )
            }
        }

        @Volatile
        private var INSTANCE: CropCareDatabase? = null

        fun getDatabase(context: Context): CropCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CropCareDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_6_7)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}