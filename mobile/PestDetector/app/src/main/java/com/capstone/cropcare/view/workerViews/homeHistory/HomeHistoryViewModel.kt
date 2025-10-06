package com.capstone.cropcare.view.workerViews.homeHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.ReportModel
import com.capstone.cropcare.domain.usecase.GetReportHistoryUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getReportHistoryUseCase: GetReportHistoryUseCase
) : ViewModel() {

    private val _reports = MutableStateFlow<List<ReportModel>>(emptyList())
    val reports: StateFlow<List<ReportModel>> = _reports.asStateFlow()

    init {
        loadReports()
    }

    private fun loadReports() {
        viewModelScope.launch {
            getReportHistoryUseCase().collect { list ->
                _reports.value = list
            }
        }
    }
}
