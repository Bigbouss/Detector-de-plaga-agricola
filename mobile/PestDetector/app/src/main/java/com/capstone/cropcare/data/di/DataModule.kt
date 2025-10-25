package com.capstone.cropcare.data.di


import com.capstone.cropcare.data.remote.api.InvitationApiService
import com.capstone.cropcare.data.repository.InvitationRepositoryImpl
import com.capstone.cropcare.domain.repository.InvitationRepository
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
}