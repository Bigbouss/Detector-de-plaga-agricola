package com.capstone.cropcare.domain.usecase

import android.util.Log
import com.capstone.cropcare.domain.repository.ScanResultRepository
import com.capstone.cropcare.domain.repository.ScanSessionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class SyncScanDataUseCase @Inject constructor(
    private val scanSessionRepository: ScanSessionRepository,
    private val scanResultRepository: ScanResultRepository
) {

    suspend operator fun invoke(): Result<SyncSummary> {
        return try {
            coroutineScope {
                // Sincronizar sesiones y resultados en paralelo
                val sessionsResult = async { scanSessionRepository.syncSessionsWithBackend() }
                val resultsResult = async { scanResultRepository.syncScanResultsWithBackend() }

                // Esperar ambos resultados
                val sessionSuccess = sessionsResult.await().isSuccess
                val resultSuccess = resultsResult.await().isSuccess

                val summary = SyncSummary(
                    sessionsSuccess = sessionSuccess,
                    resultsSuccess = resultSuccess,
                    totalSuccess = sessionSuccess && resultSuccess
                )

                if (summary.totalSuccess) {
                    Log.d("SyncScanData", "✅ Sincronización completa exitosa")
                    Result.success(summary)
                } else {
                    Log.w("SyncScanData", "⚠️ Sincronización parcial: $summary")
                    Result.success(summary)
                }
            }
        } catch (e: Exception) {
            Log.e("SyncScanData", "❌ Error en sincronización", e)
            Result.failure(e)
        }
    }

    suspend fun hasPendingSync(): Boolean {
        return try {
            val unsyncedSessions = scanSessionRepository.getUnsyncedSessions()
            val unsyncedResults = scanResultRepository.getUnsyncedScanResults()
            unsyncedSessions.isNotEmpty() || unsyncedResults.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}

data class SyncSummary(
    val sessionsSuccess: Boolean,
    val resultsSuccess: Boolean,
    val totalSuccess: Boolean
)