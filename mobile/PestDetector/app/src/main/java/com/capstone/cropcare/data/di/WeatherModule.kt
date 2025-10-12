package com.capstone.cropcare.data.di

import android.content.Context
import coil3.ImageLoader
import coil3.svg.SvgDecoder
import com.capstone.cropcare.data.remote.api.WeatherApiService
import com.capstone.cropcare.data.repository.WeatherRepositoryImpl
import com.capstone.cropcare.domain.repository.WeatherRepository
import com.capstone.cropcare.domain.usecase.GetWeatherUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {

    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    // --- Retrofit ---
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- API Service ---
    @Provides
    @Singleton
    fun provideWeatherApi(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    // --- Repository ---
    @Provides
    @Singleton
    fun provideWeatherRepository(api: WeatherApiService): WeatherRepository {
        return WeatherRepositoryImpl(api)
    }

    // --- Use Case ---
    @Provides
    @Singleton
    fun provideGetWeatherUseCase(repository: WeatherRepository): GetWeatherUseCase {
        return GetWeatherUseCase(repository)
    }

    // --- Coil ImageLoader ---
    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
}