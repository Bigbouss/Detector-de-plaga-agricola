package com.capstone.cropcare.view.workerViews.homeHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.entity.ReportHistory
import com.capstone.cropcare.domain.usecase.HomeHistoryUserCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getReportHistoryUseCase: HomeHistoryUserCase) : ViewModel() {

    private val _reports = MutableStateFlow<List<ReportHistory>>(emptyList())
    val reports: StateFlow<List<ReportHistory>> = _reports

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _reports.value = getReportHistoryUseCase()
        }
    }

}