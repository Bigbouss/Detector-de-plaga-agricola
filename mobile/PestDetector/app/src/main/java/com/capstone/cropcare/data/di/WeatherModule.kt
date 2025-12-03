package com.capstone.cropcare.data.di

import android.content.Context
import coil3.ImageLoader
import coil3.svg.SvgDecoder
import com.capstone.cropcare.data.remote.api.WeatherApiService
import com.capstone.cropcare.data.repository.WeatherRepositoryImpl
import com.capstone.cropcare.domain.repository.WeatherRepository
import com.capstone.cropcare.domain.usecase.weatherUseCase.GetWeatherUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {
    //url de api openWeather
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    @Provides @Singleton
    @Named("weatherRetrofit")
    fun provideWeatherRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides @Singleton
    fun provideWeatherApi(@Named("weatherRetrofit") retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    @Provides @Singleton
    fun provideWeatherRepository(api: WeatherApiService): WeatherRepository {
        return WeatherRepositoryImpl(api)
    }

    @Provides @Singleton
    fun provideGetWeatherUseCase(repository: WeatherRepository): GetWeatherUseCase {
        return GetWeatherUseCase(repository)
    }
}
