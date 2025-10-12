package com.capstone.cropcare.view.workerViews.reports

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.model.ReportModel
import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import com.capstone.cropcare.domain.repository.ReportRepository
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
    private val app: Application
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    // Lista de zonas disponibles
    private val _availableZones = MutableStateFlow<List<ZoneModel>>(emptyList())
    val availableZones: StateFlow<List<ZoneModel>> = _availableZones.asStateFlow()

    // Cultivos de la zona seleccionada
    private val _availableCrops = MutableStateFlow<List<CropModel>>(emptyList())
    val availableCrops: StateFlow<List<CropModel>> = _availableCrops.asStateFlow()

    init {
        loadZones()
    }

    private fun loadZones() {
        viewModelScope.launch {
            cropZoneRepository.getAllZones().collect { zones ->
                _availableZones.value = zones
            }
        }
    }

    // 👇 Cuando selecciona una zona, carga sus cultivos
    fun selectZone(zone: ZoneModel) {
        _state.update { it.copy(selectedZone = zone, selectedCrop = null) }

        viewModelScope.launch {
            cropZoneRepository.getCropsByZone(zone.id).collect { crops ->
                _availableCrops.value = crops
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

        // Validaciones
        if (current.selectedZone == null) {
            Log.e("ReportViewModel", "❌ Zona no seleccionada")
            return@launch
        }
        if (current.selectedCrop == null) {
            Log.e("ReportViewModel", "❌ Cultivo no seleccionado")
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
        } catch (e: Exception) {
            Log.e("ReportViewModel", "❌ Error guardando reporte", e)
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
}

data class ReportState(
    val workerName: String = "",
    val diagnostic: String = "",
    val selectedZone: ZoneModel? = null, // 👈 Cambió de String a Zone
    val selectedCrop: CropModel? = null, // 👈 Nuevo campo
    val analizedPhoto: Bitmap? = null,
    val localPhotoPath: String? = null,
    val observation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)