package com.capstone.cropcare.data.di

import com.capstone.cropcare.data.local.dao.WorkerZoneAssignmentDao
import com.capstone.cropcare.data.local.dao.ZoneDao
import com.capstone.cropcare.data.remote.api.InvitationApiService
import com.capstone.cropcare.data.remote.api.WorkersApiService
import com.capstone.cropcare.data.repository.InvitationRepositoryImpl
import com.capstone.cropcare.data.repository.WorkersRepositoryImpl
import com.capstone.cropcare.domain.repository.InvitationRepository
import com.capstone.cropcare.domain.repository.WorkersRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideInvitationRepository(
        apiService: InvitationApiService,
        getCurrentUserUseCase: com.capstone.cropcare.domain.usecase.authUseCase.GetCurrentUserUseCase
    ): InvitationRepository {
        return InvitationRepositoryImpl(apiService, getCurrentUserUseCase)
    }

    @Provides
    @Singleton
    fun provideWorkersRepository(
        apiService: WorkersApiService,
        assignmentDao: WorkerZoneAssignmentDao,
        zoneDao: ZoneDao
    ): WorkersRepository {
        return WorkersRepositoryImpl(apiService, assignmentDao, zoneDao)
    }
}