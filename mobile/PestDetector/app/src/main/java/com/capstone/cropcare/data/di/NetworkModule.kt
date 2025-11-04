package com.capstone.cropcare.data.di

import com.capstone.cropcare.data.local.preferences.TokenManager
import com.capstone.cropcare.data.remote.api.AuthApiService
import com.capstone.cropcare.data.remote.api.InvitationApiService
import com.capstone.cropcare.data.remote.api.WorkersApiService
import com.capstone.cropcare.data.remote.api.ZonesApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    const val BASE_URL = "http://192.168.2.114:8000/api/"

    @Provides @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        tokenManager: TokenManager
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                val token = tokenManager.getAccessToken()
                val newRequest = if (token != null &&
                    !request.url.encodedPath.contains("/auth/token") &&
                    !request.url.encodedPath.contains("/auth/register")) {
                    request.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else request
                chain.proceed(newRequest)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit compartido (ambos servicios usan el mismo)
    @Provides @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // AuthApiService
    @Provides @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    // InvitationApiService
    @Provides @Singleton
    fun provideInvitationApiService(retrofit: Retrofit): InvitationApiService {
        return retrofit.create(InvitationApiService::class.java)
    }

    // WorkersApiService
    @Provides @Singleton
    fun provideWorkersApiService(retrofit: Retrofit): WorkersApiService {
        return retrofit.create(WorkersApiService::class.java)
    }

    // ZonesApiService
    @Provides @Singleton
    fun provideZonesApiService(retrofit: Retrofit): ZonesApiService {
        return retrofit.create(ZonesApiService::class.java)
    }
}