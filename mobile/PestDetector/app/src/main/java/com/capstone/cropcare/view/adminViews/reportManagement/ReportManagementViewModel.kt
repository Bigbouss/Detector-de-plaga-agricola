package com.capstone.cropcare.view.adminViews.reportManagement

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.data.remote.api.ScannerApiService
import com.capstone.cropcare.data.remote.dto.SessionWithReportDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class GroupBy {
    DATE, WEEK, MONTH, WORKER
}

@HiltViewModel
class ReportManagementViewModel @Inject constructor(
    private val scannerApiService: ScannerApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportManagementState())
    val uiState: StateFlow<ReportManagementState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val response = scannerApiService.getSessionsWithReports()

                if (response.isSuccessful) {
                    val sessions = response.body() ?: emptyList()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sessions = sessions,
                            groupedSessions = groupSessions(sessions, it.groupBy, it.filterWithPlagues)
                        )
                    }

                    Log.d("ReportManagementVM", "${sessions.size} sesiones con reportes cargadas")
                } else {
                    throw Exception("Error ${response.code()}: ${response.message()}")
                }

            } catch (e: Exception) {
                Log.e("ReportManagementVM", "Error cargando sesiones", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar reportes: ${e.message}"
                    )
                }
            }
        }
    }

    fun setGroupBy(groupBy: GroupBy) {
        _uiState.update {
            it.copy(
                groupBy = groupBy,
                groupedSessions = groupSessions(it.sessions, groupBy, it.filterWithPlagues)
            )
        }
    }

    fun filterByPlagueStatus(withPlagues: Boolean?) {
        _uiState.update {
            it.copy(
                filterWithPlagues = withPlagues,
                groupedSessions = groupSessions(it.sessions, it.groupBy, withPlagues)
            )
        }
    }

    private fun groupSessions(
        sessions: List<SessionWithReportDTO>,
        groupBy: GroupBy,
        filterWithPlagues: Boolean?
    ): Map<String, List<SessionWithReportDTO>> {

        // Filtrar primero
        val filtered = when (filterWithPlagues) {
            true -> sessions.filter { it.report?.suspiciousFlag == true }
            false -> sessions.filter { it.report?.suspiciousFlag == false }
            null -> sessions
        }

        // Luego agrupar
        return when (groupBy) {
            GroupBy.DATE -> filtered.groupBy {
                formatDateGroup(it.finishedAt ?: it.startedAt)
            }
            GroupBy.WEEK -> filtered.groupBy {
                getWeekLabel(it.finishedAt ?: it.startedAt)
            }
            GroupBy.MONTH -> filtered.groupBy {
                getMonthLabel(it.finishedAt ?: it.startedAt)
            }
            GroupBy.WORKER -> filtered.groupBy { it.workerName }
        }.toSortedMap(compareByDescending { it })
    }

    private fun formatDateGroup(isoDate: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = format.parse(isoDate)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            isoDate
        }
    }

    private fun getWeekLabel(isoDate: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = format.parse(isoDate) ?: Date()
            val calendar = Calendar.getInstance()
            calendar.time = date
            val week = calendar.get(Calendar.WEEK_OF_YEAR)
            val year = calendar.get(Calendar.YEAR)
            "Semana $week, $year"
        } catch (e: Exception) {
            isoDate
        }
    }

    private fun getMonthLabel(isoDate: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val date = format.parse(isoDate)
            outputFormat.format(date ?: Date()).capitalize(Locale.getDefault())
        } catch (e: Exception) {
            isoDate
        }
    }
}

data class ReportManagementState(
    val isLoading: Boolean = false,
    val sessions: List<SessionWithReportDTO> = emptyList(),
    val groupedSessions: Map<String, List<SessionWithReportDTO>> = emptyMap(),
    val groupBy: GroupBy = GroupBy.DATE,
    val filterWithPlagues: Boolean? = null,
    val error: String? = null
)