package com.capstone.cropcare.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.capstone.cropcare.domain.usecase.SyncScanDataUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncScanDataUseCase: SyncScanDataUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Iniciando sincronización en segundo plano...")

            val syncResult = syncScanDataUseCase()

            if (syncResult.isSuccess) {
                val summary = syncResult.getOrNull()
                Log.d(TAG, "Sincronización exitosa: $summary")
                Result.success()
            } else {
                Log.e(TAG, "Error en sincronización", syncResult.exceptionOrNull())
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ejecutando worker", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SyncScanWorker"
        const val WORK_NAME = "sync_scan_data"

        fun schedule(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<SyncScanWorker>(
                15, TimeUnit.MINUTES  // Sincronizar cada 15 minutos
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15, TimeUnit.MINUTES
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

            Log.d(TAG, "Sincronización periódica programada")
        }

        fun syncNow(workManager: WorkManager) {
            val workRequest = OneTimeWorkRequestBuilder<SyncScanWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            workManager.enqueueUniqueWork(
                "sync_scan_data_now",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d(TAG, "Sincronización inmediata iniciada")
        }
    }
}