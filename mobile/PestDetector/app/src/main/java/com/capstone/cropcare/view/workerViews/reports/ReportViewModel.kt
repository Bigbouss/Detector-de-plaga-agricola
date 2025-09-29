package com.capstone.cropcare.view.workerViews.reports

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor() : ViewModel() {

    // Estado del reporte
    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    init {
        //fecha y hora al iniciar
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)

        _state.update { it.copy(date = date, time = time) }
    }


    fun setWorkerName(name: String) {
        _state.update { it.copy(workerName = name) }
    }

    fun setDiagnostic(diagnostic: String) {
        _state.update { it.copy(diagnostic = diagnostic) }
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
}


data class ReportState(
    val date: String = "",
    val time: String = "",
    val workerName: String = "",
    val diagnostic: String = "",
    val cropZone: String = "",
    val analizedPhoto: Bitmap? = null,
    val observation: String = ""
)
