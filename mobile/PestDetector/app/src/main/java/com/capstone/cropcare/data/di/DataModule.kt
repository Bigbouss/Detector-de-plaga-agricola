package com.capstone.cropcare.data.di

import com.capstone.cropcare.data.repository.AuthRepositoryImpl
import com.capstone.cropcare.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    fun provideAuthRepository(): AuthRepository{
        return AuthRepositoryImpl()
    }


}