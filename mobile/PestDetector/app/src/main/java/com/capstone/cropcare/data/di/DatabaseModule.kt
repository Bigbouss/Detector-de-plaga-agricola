package com.capstone.cropcare.data.di

import android.content.Context
import androidx.room.Room
import com.capstone.cropcare.data.local.dao.CropDao
import com.capstone.cropcare.data.local.dao.ReportDao
import com.capstone.cropcare.data.local.dao.ZoneDao
import com.capstone.cropcare.data.local.database.CropCareDatabase
import com.capstone.cropcare.data.repository.CropZoneRepositoryImpl
import com.capstone.cropcare.data.repository.ReportRepositoryImpl
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
    @Singleton
    fun provideCropZoneRepository(
        zoneDao: ZoneDao,
        cropDao: CropDao
    ): CropZoneRepository {
        return CropZoneRepositoryImpl(zoneDao, cropDao)
    }

    @Provides
    @Singleton
    fun provideReportRepository(
        reportDao: ReportDao
    ): ReportRepository {
        return ReportRepositoryImpl(reportDao)
    }
}