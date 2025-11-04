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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first  // ✅ Agregar este import
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
    private val app: Application
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    private val _availableZones = MutableStateFlow<List<ZoneModel>>(emptyList())
    val availableZones: StateFlow<List<ZoneModel>> = _availableZones.asStateFlow()

    private val _availableCrops = MutableStateFlow<List<CropModel>>(emptyList())
    val availableCrops: StateFlow<List<CropModel>> = _availableCrops.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadAssignedZones()
        loadCurrentWorkerName()
    }

    private fun loadCurrentWorkerName() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    setWorkerName(user.name)
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "❌ Error obteniendo nombre del trabajador", e)
            }
        }
    }

    private fun loadAssignedZones() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // ✅ Obtener zonas asignadas directamente desde el backend
                val result = cropZoneRepository.getAssignedZonesForCurrentWorker()

                if (result.isSuccess) {
                    val zones = result.getOrNull() ?: emptyList()
                    _availableZones.value = zones
                    Log.d("ReportViewModel", "✅ Zonas asignadas cargadas: ${zones.map { it.name }}")
                } else {
                    Log.e("ReportViewModel", "❌ Error cargando zonas asignadas")
                    _errorMessage.value = "No se pudieron cargar las zonas asignadas"
                }

            } catch (e: Exception) {
                Log.e("ReportViewModel", "❌ Error cargando zonas asignadas", e)
                _errorMessage.value = "Error al cargar zonas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectZone(zone: ZoneModel) {
        _state.update { it.copy(selectedZone = zone, selectedCrop = null) }
        _availableCrops.value = emptyList()

        viewModelScope.launch {
            try {
                // Sincronizar cultivos desde backend
                val syncResult = cropZoneRepository.syncCropsForZone(zone.id)

                if (syncResult.isSuccess) {
                    Log.d("ReportViewModel", "✅ Cultivos sincronizados para zona ${zone.name}")
                }

                // ✅ CAMBIO: Usar first() en lugar de collect()
                val crops = cropZoneRepository.getCropsByZone(zone.id).first()
                _availableCrops.value = crops
                Log.d("ReportViewModel", "🌾 Cultivos cargados: ${crops.map { it.name }}")

            } catch (e: Exception) {
                Log.e("ReportViewModel", "❌ Error cargando cultivos", e)
                _errorMessage.value = "Error al cargar cultivos: ${e.message}"
            }
        }
    }

    fun selectCrop(crop: CropModel) {
        _state.update { it.copy(selectedCrop = crop) }
    }

    fun setWorkerName(name: String) {
        _state.update { it.copy(workerName = name) }
    }

    fun setDiagnostic(diagnostic: String) {
        _state.update { it.copy(diagnostic = diagnostic) }
    }

    fun setLocalPhotoPath(path: String) {
        _state.update { it.copy(localPhotoPath = path) }
    }

    fun setAnalizedPhoto(bitmap: Bitmap) {
        _state.update { it.copy(analizedPhoto = bitmap) }
    }

    fun setObservation(observation: String) {
        _state.update { it.copy(observation = observation) }
    }

    fun saveReport() = viewModelScope.launch {
        val current = _state.value

        if (current.selectedZone == null) {
            Log.e("ReportViewModel", "❌ Zona no seleccionada")
            _errorMessage.value = "Debes seleccionar una zona"
            return@launch
        }
        if (current.selectedCrop == null) {
            Log.e("ReportViewModel", "❌ Cultivo no seleccionado")
            _errorMessage.value = "Debes seleccionar un cultivo"
            return@launch
        }

        val finalPath = current.localPhotoPath ?: saveBitmapToLocalPath(current.analizedPhoto)

        val report = ReportModel(
            workerName = current.workerName,
            diagnostic = current.diagnostic,
            zone = current.selectedZone,
            crop = current.selectedCrop,
            photoPath = finalPath,
            observation = current.observation,
            timestamp = current.timestamp,
            syncedWithBackend = false
        )

        try {
            reportRepository.insertReport(report)
            Log.d("ReportViewModel", "✅ Reporte guardado exitosamente")
            _errorMessage.value = null
        } catch (e: Exception) {
            Log.e("ReportViewModel", "❌ Error guardando reporte", e)
            _errorMessage.value = "Error al guardar reporte: ${e.message}"
        }
    }

    private fun saveBitmapToLocalPath(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        return try {
            val filename = "report_${System.currentTimeMillis()}.jpg"
            val file = File(app.filesDir, filename)
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("ReportViewModel", "❌ Error guardando bitmap", e)
            null
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

data class ReportState(
    val workerName: String = "",
    val diagnostic: String = "",
    val selectedZone: ZoneModel? = null,
    val selectedCrop: CropModel? = null,
    val analizedPhoto: Bitmap? = null,
    val localPhotoPath: String? = null,
    val observation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)