package com.capstone.cropcare.data.di

import com.capstone.cropcare.data.local.preferences.TokenManager
import com.capstone.cropcare.data.remote.api.AuthApiService
import com.capstone.cropcare.data.remote.api.InvitationApiService
import com.capstone.cropcare.data.remote.api.ReportApiService
import com.capstone.cropcare.data.remote.api.ScannerApiService  // ✅ NUEVO
import com.capstone.cropcare.data.remote.api.WorkersApiService
import com.capstone.cropcare.data.remote.api.ZonesApiService
import com.capstone.cropcare.data.remote.interceptors.AuthInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    //<-- url backend local -->
    //private const val BASE_URL = "http://192.168.2.114:8000/"
    //<-- url backend en AWS -->
    private const val BASE_URL = "http://52.70.81.198:8000/"

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideInvitationApiService(retrofit: Retrofit): InvitationApiService {
        return retrofit.create(InvitationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkersApiService(retrofit: Retrofit): WorkersApiService {
        return retrofit.create(WorkersApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideZonesApiService(retrofit: Retrofit): ZonesApiService {
        return retrofit.create(ZonesApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideScannerApiService(retrofit: Retrofit): ScannerApiService {
        return retrofit.create(ScannerApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideReportApiService(retrofit: Retrofit): ReportApiService {
        return retrofit.create(ReportApiService::class.java)
    }
}