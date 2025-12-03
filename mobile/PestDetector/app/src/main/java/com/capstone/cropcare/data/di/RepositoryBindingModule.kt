package com.capstone.cropcare.data.di

import com.capstone.cropcare.domain.repository.ScanSessionRepository
import com.capstone.cropcare.data.repository.ScanSessionRepositoryImpl
import com.capstone.cropcare.domain.repository.ScanResultRepository
import com.capstone.cropcare.data.repository.ScanResultRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingModule {

    @Binds
    @Singleton
    abstract fun bindScanSessionRepository(
        impl: ScanSessionRepositoryImpl
    ): ScanSessionRepository

    @Binds
    @Singleton
    abstract fun bindScanResultRepository(
        impl: ScanResultRepositoryImpl
    ): ScanResultRepository
}
