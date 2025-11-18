//package com.capstone.cropcare.data.di
//
//import com.capstone.cropcare.data.repository.ScanSessionRepositoryImpl
//import com.capstone.cropcare.domain.repository.ScanSessionRepository
//import dagger.Binds
//import dagger.Module
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//abstract class ScanModule {
//
//    @Binds
//    @Singleton
//    abstract fun bindScanSessionRepository(
//        impl: ScanSessionRepositoryImpl
//    ): ScanSessionRepository
//}