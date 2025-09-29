package com.capstone.cropcare.data.di

import com.capstone.cropcare.domain.repository.ReportHistoryRepository
import com.capstone.cropcare.data.repository.FakeReportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryHistoryModule {

    @Binds
    @Singleton
    abstract fun bindReportHistoryRepository(
        fakeReportRepository: FakeReportRepository
    ): ReportHistoryRepository
}