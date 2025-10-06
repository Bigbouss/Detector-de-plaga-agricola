package com.capstone.cropcare.data.di

import android.content.Context
import androidx.room.Room
import com.capstone.cropcare.data.local.database.AppDatabase
import com.capstone.cropcare.data.local.dao.ReportDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideRoomDatabase(@ApplicationContext appContext: Context): AppDatabase{
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java, name = "cropcare_local_database"
        ).build()
    }

    @Singleton
    @Provides
    fun provideReportDao(db: AppDatabase): ReportDao = db.reportDao()
}