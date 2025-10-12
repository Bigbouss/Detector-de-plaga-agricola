package com.capstone.cropcare.view.workerViews.homeHistory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.ReportModel
import com.capstone.cropcare.domain.repository.ReportRepository
import com.capstone.cropcare.domain.usecase.GetReportHistoryUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//@HiltViewModel
//class HistoryViewModel @Inject constructor(
//    private val getReportHistoryUseCase: GetReportHistoryUseCase
//) : ViewModel() {
//
//    private val _reports = MutableStateFlow<List<ReportModel>>(emptyList())
//    val reports: StateFlow<List<ReportModel>> = _reports.asStateFlow()
//
//    init {
//        loadReports()
//    }
//
//    private fun loadReports() {
//        viewModelScope.launch {
//            getReportHistoryUseCase().collect { list ->
//                _reports.value = list
//            }
//        }
//    }
//}
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _reports = MutableStateFlow<List<ReportModel>>(emptyList()) // 👈 Cambió de ReportModel a Report
    val reports: StateFlow<List<ReportModel>> = _reports.asStateFlow()

    init {
        loadReports()
    }

    private fun loadReports() {
        viewModelScope.launch {
            reportRepository.getAllReports().collect { reportList ->
                _reports.value = reportList
            }
        }
    }

    fun deleteReport(report: ReportModel) {
        viewModelScope.launch {
            try {
                reportRepository.deleteReport(report)
                Log.d("HistoryViewModel", "✅ Reporte eliminado")
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "❌ Error eliminando reporte", e)
            }
        }
    }
}