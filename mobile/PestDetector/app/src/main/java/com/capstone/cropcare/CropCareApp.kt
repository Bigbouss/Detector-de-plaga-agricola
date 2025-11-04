package com.capstone.cropcare

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CropCareApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // La app ya está lista, ahora usa datos del backend
    }
}