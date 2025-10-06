package com.capstone.cropcare.view.workerViews.reports

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.ReportModel
import com.capstone.cropcare.domain.repository.ReportRepository
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: ReportRepository,
    private val app: Application
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    fun setWorkerName(name: String) {
        _state.update { it.copy(workerName = name) }
    }


    fun setDiagnostic(diagnostic: String) {
        _state.update { it.copy(diagnostic = diagnostic) }
    }
    fun setLocalPhotoPath(path: String) {
        _state.update { it.copy(localPhotoPath = path) }
    }

    fun setCropZone(zone: String) {
        _state.update { it.copy(cropZone = zone) }
    }

    fun setAnalizedPhoto(bitmap: Bitmap) {
        _state.update { it.copy(analizedPhoto = bitmap) }
    }

    fun setObservation(observation: String) {
        _state.update { it.copy(observation = observation) }
    }

    fun saveReport() = viewModelScope.launch {

        val current = _state.value
        val finalPath = current.localPhotoPath ?: saveBitmapToLocalPath(current.analizedPhoto)

        val reportModel = ReportModel(
            workerName = current.workerName,
            diagnostic = current.diagnostic,
            cropZone = current.cropZone,
            localPhotoPath = finalPath,
            observation = current.observation,
            timestamp = current.timestamp
        )

        try {
            repository.insertReport(reportModel)
            Log.d("ReportViewModel", "Reporte guardado exitosamente con path: $finalPath")
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error guardando reporte", e)
        }
    }


    // Función auxiliar para convertir Bitmap a path local (si quieres guardar fotos)
    private fun saveBitmapToLocalPath(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        return try {
            val filename = "report_${System.currentTimeMillis()}.jpg"
            val file = File(app.filesDir, filename)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.flush()
            stream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}



data class ReportState(
    val workerName: String = "",
    val diagnostic: String = "",
    val cropZone: String = "",
    val analizedPhoto: Bitmap? = null,
    val localPhotoPath: String? = null, // <-- agregado
    val observation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

