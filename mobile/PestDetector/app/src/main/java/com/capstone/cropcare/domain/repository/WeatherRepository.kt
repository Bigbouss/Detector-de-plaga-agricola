package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.data.remote.dto.WeatherResponse

interface WeatherRepository {
    suspend fun getWeatherByCoords(lat: Double, lon: Double): WeatherResponse
}
