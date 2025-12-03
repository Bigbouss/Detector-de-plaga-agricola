package com.capstone.cropcare.view.workerViews.reports

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.model.ReportModel
import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.AuthRepository
import com.capstone.cropcare.domain.repository.CropZoneRepository
import com.capstone.cropcare.domain.repository.ReportRepository
import com.capstone.cropcare.domain.repository.ScanResultRepository
import com.capstone.cropcare.domain.repository.ScanSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val cropZoneRepository: CropZoneRepository,
    private val authRepository: AuthRepository,
    private val scanSessionRepository: ScanSessionRepository,
    private val scanResultRepository: ScanResultRepository,
    private val app: Application
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCurrentWorkerInfo()
    }

    private fun loadCurrentWorkerInfo() {
        viewModelScope.launch {
            try {
                authRepository.getCurrentUser()?.let { user ->
                    val displayName = user.email.substringBefore("@")

                    _state.update { s ->
                        s.copy(
                            workerName = displayName,
                            workerId = user.id
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error obteniendo info del trabajador", e)
            }
        }
    }


    fun loadFromScan(sessionId: String, scanResultId: String) {
        viewModelScope.launch {
            try {
                val session = scanSessionRepository.getSessionById(sessionId)
                val scan = scanResultRepository.getScanResultById(scanResultId)

                if (session != null && scan != null) {
                    _state.update { s ->
                        s.copy(
                            selectedZone = ZoneModel(
                                id = session.zoneId,
                                name = session.zoneName
                            ),
                            selectedCrop = CropModel(
                                id = session.cropId,
                                name = session.cropName,
                                zoneId = session.zoneId
                            ),
                            diagnostic = scan.classification,
                            localPhotoPath = scan.photoPath,
                            observation = "",
                            sessionId = sessionId,
                            scanResultId = scanResultId
                        )
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error cargando datos de escaneo: ${e.message}"
            }
        }
    }

    fun setDiagnostic(diagnostic: String) {
        _state.update { it.copy(diagnostic = diagnostic) }
    }

    fun setObservation(text: String) {
        _state.update { it.copy(observation = text) }
    }

    fun setAnalizedPhoto(bitmap: Bitmap) {
        _state.update { it.copy(analizedPhoto = bitmap) }
    }

    fun setLocalPhotoPath(path: String) {
        _state.update { it.copy(localPhotoPath = path) }
    }

    /**
     * Guarda el reporte en Room (modo offline-first)
     */
    fun saveReport() = viewModelScope.launch {
        val current = _state.value

        if (current.selectedZone == null || current.selectedCrop == null) {
            _errorMessage.value = "Error interno: Zona o cultivo faltan en el reporte"
            return@launch
        }

        val finalPath = current.localPhotoPath ?: saveBitmap(current.analizedPhoto)

        val report = ReportModel(
            id = 0, // autogen por Room
            workerName = current.workerName,
            workerId = current.workerId,
            diagnostic = current.diagnostic,
            confidence = null,
            zone = current.selectedZone,
            crop = current.selectedCrop,
            photoPath = finalPath,
            observation = current.observation,
            timestamp = current.timestamp,
            sessionId = current.sessionId,
            scanResultId = current.scanResultId,
            syncedWithBackend = false
        )

        try {
            reportRepository.insertReport(report)
            Log.d("ReportViewModel", "Reporte guardado correctamente")
        } catch (e: Exception) {
            _errorMessage.value = "Error guardando reporte: ${e.message}"
        }
    }

    private fun saveBitmap(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        return try {
            val file = File(app.filesDir, "report_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
            file.absolutePath
        } catch (e: Exception) {
            _errorMessage.value = "Error guardando imagen: ${e.message}"
            null
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

data class ReportState(
    val workerName: String = "",
    val workerId: Int = -1,
    val diagnostic: String = "",
    val selectedZone: ZoneModel? = null,
    val selectedCrop: CropModel? = null,
    val analizedPhoto: Bitmap? = null,
    val localPhotoPath: String? = null,
    val observation: String = "",
    val sessionId: String? = null,
    val scanResultId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)