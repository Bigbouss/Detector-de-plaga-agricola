package com.capstone.cropcare.domain.utils

import android.content.Context
import com.capstone.cropcare.R

fun formatWeatherDescription(description: String, context: Context): String {
    return when (description.lowercase()) {
        "cielo claro", "clear sky" -> context.getString(R.string.weather_clear)
        "few clouds", "pocas nubes" -> context.getString(R.string.weather_partly_cloudy)
        "scattered clouds", "nubes dispersas" -> context.getString(R.string.weather_cloudy)
        "broken clouds" -> context.getString(R.string.weather_very_cloudy)
        "shower rain", "lluvia ligera" -> context.getString(R.string.weather_light_rain)
        "rain", "lluvia" -> context.getString(R.string.weather_rain)
        "thunderstorm", "tormenta" -> context.getString(R.string.weather_storm)
        "snow", "nieve" -> context.getString(R.string.weather_snow)
        "mist", "niebla" -> context.getString(R.string.weather_fog)
        else -> description.replaceFirstChar { it.uppercase() }
    }
}
