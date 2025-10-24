package com.capstone.cropcare.data.di

import com.capstone.cropcare.data.repository.AuthRepositoryImpl
import com.capstone.cropcare.data.repository.FakeAuthRepository
import com.capstone.cropcare.data.repository.FakeInvitationRepository
import com.capstone.cropcare.domain.repository.AuthRepository
import com.capstone.cropcare.domain.repository.InvitationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

//    @Binds
//    @Singleton
//    abstract fun bindAuthRepository(
//        authRepositoryImpl: AuthRepositoryImpl //
//    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindInvitationRepository(
        fakeInvitationRepository: FakeInvitationRepository
    ): InvitationRepository
}
//@Module
//@InstallIn(SingletonComponent::class)
//object AuthModule {
//
//    @Provides
//    @Singleton
//    fun provideAuthRepository(
//        fakeAuthRepository: FakeAuthRepository // Fake
//    ): AuthRepository {
//        return fakeAuthRepository
//    }
//
//    // 👇 Cuando tengas el backend real, cambia a:
//    /*
//    @Provides
//    @Singleton
//    fun provideAuthRepository(
//        authRepositoryImpl: AuthRepositoryImpl
//    ): AuthRepository {
//        return authRepositoryImpl
//    }
//    */
//}