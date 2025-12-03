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
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _groups = MutableStateFlow<List<ReportSessionGroup>>(emptyList())
    val groups: StateFlow<List<ReportSessionGroup>> = _groups.asStateFlow()

    init {
        loadReports()
    }

    private fun loadReports() {
        viewModelScope.launch {
            reportRepository.getAllReports().collect { reportList ->

                // Agrupamos por sessionId (null incluido)
                val grouped = reportList
                    .groupBy { it.sessionId }
                    .map { (sessionId, reports) ->
                        val first = reports.first()

                        ReportSessionGroup(
                            sessionId = sessionId,
                            title = "${first.zone.name} - ${first.crop.name}",
                            date = reports.minOf { it.timestamp },
                            reports = reports.sortedByDescending { it.timestamp }
                        )
                    }
                    .sortedByDescending { it.date }

                _groups.value = grouped
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

data class ReportSessionGroup(
    val sessionId: String?,
    val title: String,
    val date: Long,
    val reports: List<ReportModel>
)
