package com.capstone.cropcare.view.workerViews.analysisResult

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AnalysisState {
    object Loading : AnalysisState()
    object Good : AnalysisState()
    object Bad : AnalysisState()
    object TryAgain : AnalysisState()
}

@HiltViewModel
class AnalysisViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<AnalysisState>(AnalysisState.Loading)
    val state: StateFlow<AnalysisState> = _state

    fun setAnalysisResult(result: AnalysisState) {
        _state.value = result
    }

    init {
        // Simulación de analisis
        viewModelScope.launch {
            delay(2000)
            _state.value = AnalysisState.Good //estado

        }
    }

    // Simulación de análisis photo
    fun analyzePhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.value = AnalysisState.Loading
            delay(1500) // Simula proceso


            _state.value = AnalysisState.Good
        }
    }
}


