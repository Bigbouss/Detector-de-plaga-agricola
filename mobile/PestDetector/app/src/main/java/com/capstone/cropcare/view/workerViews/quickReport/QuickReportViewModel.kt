//package com.capstone.cropcare.view.workerViews.quickReport
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.capstone.cropcare.domain.model.ReportModel
//import com.capstone.cropcare.domain.model.ScanResultModel
//import com.capstone.cropcare.domain.model.ScanSessionModel
//import com.capstone.cropcare.domain.repository.ReportRepository
//import com.capstone.cropcare.domain.repository.ScanResultRepository
//import com.capstone.cropcare.domain.repository.ScanSessionRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class QuickReportViewModel @Inject constructor(
//    private val reportRepository: ReportRepository,
//    private val scanResultRepository: ScanResultRepository,
//    private val sessionRepository: ScanSessionRepository
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(QuickReportState())
//    val uiState: StateFlow<QuickReportState> = _uiState.asStateFlow()
//
//    private val _scanResult = MutableStateFlow<ScanResultModel?>(null)
//    val scanResult: StateFlow<ScanResultModel?> = _scanResult.asStateFlow()
//
//    private val _session = MutableStateFlow<ScanSessionModel?>(null)
//    val session: StateFlow<ScanSessionModel?> = _session.asStateFlow()
//
//    fun loadData(sessionId: String, scanResultId: String) {
//        viewModelScope.launch {
//            try {
//                // Cargar sesión
//                _session.value = sessionRepository.getSessionById(sessionId)
//
//                // Cargar resultado del escaneo
//                _scanResult.value = scanResultRepository.getScanResultById(scanResultId)
//
//                Log.d("QuickReportVM", "📋 Datos cargados - Sesión: $sessionId, Scan: $scanResultId")
//
//            } catch (e: Exception) {
//                Log.e("QuickReportVM", "❌ Error cargando datos", e)
//                _uiState.update { it.copy(error = "Error al cargar datos: ${e.message}") }
//            }
//        }
//    }
//
//    fun saveReport(observations: String, onSuccess: () -> Unit) {
//        val scan = _scanResult.value
//        val sess = _session.value
//
//        if (scan == null || sess == null) {
//            _uiState.update { it.copy(error = "Datos incompletos") }
//            return
//        }
//
//        viewModelScope.launch {
//            _uiState.update { it.copy(isSaving = true, error = null) }
//
//            try {
//                // Crear el reporte vinculado al scan result
//                val report = ReportModel(
//                    workerName = sess.workerName,
//                    diagnostic = scan.classification,
//                    confidence = scan.confidence,
//                    zone = com.capstone.cropcare.domain.model.ZoneModel(
//                        id = sess.zoneId,
//                        name = sess.zoneName,
//                        description = ""
//
//                    ),
//                    crop = com.capstone.cropcare.domain.model.CropModel(
//                        id = sess.cropId,
//                        name = sess.cropName,
//                        zoneId = sess.zoneId,
//
//                    ),
//                    photoPath = scan.photoPath,
//                    observation = observations,
//                    timestamp = System.currentTimeMillis(),
//                    sessionId = sess.id,
//                    scanResultId = scan.id
//                )
//
//                val result = reportRepository.createReport(report)
//
//                result.fold(
//                    onSuccess = { savedReport ->
//                        // Actualizar el scan result con el ID del reporte
//                        val updatedScan = scan.copy(reportId = savedReport.id.toString())
//                        scanResultRepository.updateScanResult(updatedScan)
//
//                        Log.d("QuickReportVM", "✅ Reporte guardado: ${savedReport.id}")
//                        _uiState.update { it.copy(isSaving = false) }
//                        onSuccess()
//                    },
//                    onFailure = { error ->
//                        Log.e("QuickReportVM", "❌ Error guardando reporte", error)
//                        _uiState.update {
//                            it.copy(
//                                isSaving = false,
//                                error = "Error al guardar reporte: ${error.message}"
//                            )
//                        }
//                    }
//                )
//
//            } catch (e: Exception) {
//                Log.e("QuickReportVM", "❌ Exception guardando reporte", e)
//                _uiState.update {
//                    it.copy(
//                        isSaving = false,
//                        error = "Error: ${e.message}"
//                    )
//                }
//            }
//        }
//    }
//}
//
//data class QuickReportState(
//    val isSaving: Boolean = false,
//    val error: String? = null
//)