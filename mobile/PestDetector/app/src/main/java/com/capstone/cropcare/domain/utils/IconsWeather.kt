package com.capstone.cropcare.domain.utils

import com.capstone.cropcare.R


fun getWeatherAnimation(iconCode: String): Int {
    return when (iconCode) {

        "01d" -> R.raw.weather_sunny
        "02d" -> R.raw.weather_partly_cloudy
        "03d", "04d" -> R.raw.weather_cloud
        "09d", "10d" -> R.raw.weather_partly_shower
        "11d" -> R.raw.weather_storm



        "01n" -> R.raw.weather_night
        "02n" -> R.raw.weather_cloudy_night
        "03n", "04n" -> R.raw.weather_cloud
        "09n", "10n" -> R.raw.weather_rainy_night
        "11n" -> R.raw.weather_storm


        else -> R.raw.weather_cloud
    }
}
