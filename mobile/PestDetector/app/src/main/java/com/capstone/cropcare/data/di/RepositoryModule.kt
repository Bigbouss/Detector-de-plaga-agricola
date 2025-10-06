package com.capstone.cropcare.data.di

import com.capstone.cropcare.data.local.dao.ReportDao
import com.capstone.cropcare.data.repository.ReportRepositoryImpl
import com.capstone.cropcare.domain.repository.ReportRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideReportRepository(dao: ReportDao): ReportRepository = ReportRepositoryImpl(dao)
}
