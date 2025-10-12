package com.capstone.cropcare.domain.usecase

import com.capstone.cropcare.domain.repository.WeatherRepository

class GetWeatherUseCase(private val repository: WeatherRepository) {
    suspend operator fun invoke(lat: Double, lon: Double) = repository.getWeatherByCoords(lat, lon)
}

