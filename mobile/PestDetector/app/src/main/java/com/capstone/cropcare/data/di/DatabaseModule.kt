package com.capstone.cropcare.data.di

import android.content.Context
import androidx.room.Room
import com.capstone.cropcare.data.local.dao.CropDao
import com.capstone.cropcare.data.local.dao.ReportDao
import com.capstone.cropcare.data.local.dao.ScanResultDao
import com.capstone.cropcare.data.local.dao.ScanSessionDao
import com.capstone.cropcare.data.local.dao.WorkerZoneAssignmentDao
import com.capstone.cropcare.data.local.dao.ZoneDao
import com.capstone.cropcare.data.local.database.CropCareDatabase
import com.capstone.cropcare.data.remote.api.WorkersApiService
import com.capstone.cropcare.data.remote.api.ZonesApiService
import com.capstone.cropcare.data.repository.CropZoneRepositoryImpl
import com.capstone.cropcare.data.repository.ReportRepositoryImpl
import com.capstone.cropcare.domain.repository.AuthRepository
import com.capstone.cropcare.domain.repository.CropZoneRepository
import com.capstone.cropcare.domain.repository.ReportRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // ------------------------
    // DATABASE
    // ------------------------

    @Provides
    @Singleton
    fun provideCropCareDatabase(
        @ApplicationContext context: Context
    ): CropCareDatabase {
        return Room.databaseBuilder(
            context,
            CropCareDatabase::class.java,
            "cropcare_local_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // ------------------------
    // DAOs
    // ------------------------

    @Provides
    fun provideZoneDao(db: CropCareDatabase): ZoneDao = db.zoneDao()

    @Provides
    fun provideCropDao(db: CropCareDatabase): CropDao = db.cropDao()

    @Provides
    fun provideReportDao(db: CropCareDatabase): ReportDao = db.reportDao()

    @Provides
    fun provideWorkerZoneAssignmentDao(db: CropCareDatabase): WorkerZoneAssignmentDao =
        db.workerZoneAssignmentDao()

    @Provides
    @Singleton
    fun provideScanSessionDao(db: CropCareDatabase): ScanSessionDao =
        db.scanSessionDao()

    @Provides
    @Singleton
    fun provideScanResultDao(db: CropCareDatabase): ScanResultDao =
        db.scanResultDao()

    // ------------------------
    // REPOSITORIES
    // ------------------------

    @Provides
    @Singleton
    fun provideCropZoneRepository(
        zoneDao: ZoneDao,
        cropDao: CropDao,
        zonesApiService: ZonesApiService,
        workersApiService: WorkersApiService,
        authRepository: AuthRepository
    ): CropZoneRepository {
        return CropZoneRepositoryImpl(
            zoneDao = zoneDao,
            cropDao = cropDao,
            zonesApi = zonesApiService,
            workersApi = workersApiService,
            authRepository = authRepository
        )
    }

    @Provides
    @Singleton
    fun provideReportRepository(
        reportDao: ReportDao
    ): ReportRepository {
        return ReportRepositoryImpl(reportDao)
    }
}
