package com.capstone.cropcare.data.di

import com.capstone.cropcare.data.local.preferences.TokenManager
import com.capstone.cropcare.data.local.preferences.UserPreferences
import com.capstone.cropcare.data.remote.api.AuthApiService
import com.capstone.cropcare.data.repository.AuthRepositoryImpl
import com.capstone.cropcare.domain.repository.AuthRepository
import dagger.Binds
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
    fun provideAuthRepository(
        apiService: AuthApiService,
        tokenManager: TokenManager,
        userPreferences: UserPreferences
    ): AuthRepository {
        return AuthRepositoryImpl(apiService, tokenManager, userPreferences)
    }

}