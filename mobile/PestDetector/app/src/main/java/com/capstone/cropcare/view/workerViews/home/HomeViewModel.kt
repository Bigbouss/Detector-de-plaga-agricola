package com.capstone.cropcare.view.workerViews.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.data.remote.dto.WeatherResponse
import com.capstone.cropcare.domain.usecase.GetWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase
) : ViewModel() {

    private val _weather = MutableStateFlow<WeatherResponse?>(null)
    val weather: StateFlow<WeatherResponse?> = _weather

    fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Loading weather for lat: $lat, lon: $lon")
                val result = getWeatherUseCase(lat, lon)
                Log.d("HomeViewModel", "Weather loaded: ${result.name}, icon: ${result.weather.firstOrNull()?.icon}")
                _weather.value = result
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading weather", e)
                e.printStackTrace()
            }
        }
    }

}
