package com.capstone.cropcare

import android.app.Application
import androidx.work.WorkManager
import com.capstone.cropcare.data.worker.SyncScanWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CropCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val workManager = WorkManager.getInstance(this)
        SyncScanWorker.schedule(workManager)
    }
}