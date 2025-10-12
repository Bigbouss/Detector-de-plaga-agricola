package com.capstone.cropcare


import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.capstone.cropcare.domain.usecase.SeedInitialDataUseCase
import javax.inject.Inject

@HiltAndroidApp
class CropCareApp : Application() {

    @Inject
    lateinit var seedInitialDataUseCase: SeedInitialDataUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // 👇 Siempre ejecuta seed data (solo inserta si no hay datos)
        applicationScope.launch {
            try {
                seedInitialDataUseCase()
                Log.d("CropCareApp", "✅ Seed data inicializado")
            } catch (e: Exception) {
                Log.e("CropCareApp", "❌ Error en seed data", e)
            }
        }
    }
}