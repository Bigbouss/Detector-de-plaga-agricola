package com.capstone.cropcare.data.di

import android.content.Context
import androidx.room.Room
import com.capstone.cropcare.data.local.dao.CropDao
import com.capstone.cropcare.data.local.dao.ReportDao
//import com.capstone.cropcare.data.local.dao.ScanSessionDao
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

    @Provides
    fun provideZoneDao(database: CropCareDatabase): ZoneDao {
        return database.zoneDao()
    }

    @Provides
    fun provideCropDao(database: CropCareDatabase): CropDao {
        return database.cropDao()
    }

    @Provides
    fun provideReportDao(database: CropCareDatabase): ReportDao {
        return database.reportDao()
    }

    @Provides
    fun provideWorkerZoneAssignmentDao(database: CropCareDatabase): WorkerZoneAssignmentDao {
        return database.workerZoneAssignmentDao()
    }

    @Provides
    @Singleton
    fun provideCropZoneRepository(
        zoneDao: ZoneDao,
        cropDao: CropDao,
        zonesApiService: ZonesApiService,
        workersApiService: WorkersApiService,  // ✅ Agregado
        authRepository: AuthRepository          // ✅ Agregado
    ): CropZoneRepository {
        return CropZoneRepositoryImpl(
            zoneDao = zoneDao,
            cropDao = cropDao,
            zonesApi = zonesApiService,
            workersApi = workersApiService,     // ✅ Agregado
            authRepository = authRepository     // ✅ Agregado
        )
    }

    @Provides
    @Singleton
    fun provideReportRepository(
        reportDao: ReportDao
    ): ReportRepository {
        return ReportRepositoryImpl(reportDao)
    }

//    @Provides
//    @Singleton
//    fun provideScanSessionDao(database: CropCareDatabase): ScanSessionDao {
//        return database.scanSessionDao()
//    }
}