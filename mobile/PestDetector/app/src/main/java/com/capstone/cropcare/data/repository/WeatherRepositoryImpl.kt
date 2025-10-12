package com.capstone.cropcare.data.repository

import com.capstone.cropcare.data.remote.api.WeatherApiService
import com.capstone.cropcare.data.remote.dto.WeatherResponse
import com.capstone.cropcare.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val api: WeatherApiService
) : WeatherRepository {

    private val apiKey = "96ae794b3d15226e5245236470dc165c"

    override suspend fun getWeatherByCoords(lat: Double, lon: Double): WeatherResponse {
        return api.getCurrentWeatherByCoords(
            lat = lat,
            lon = lon,
            apiKey = apiKey
        )
    }
}

