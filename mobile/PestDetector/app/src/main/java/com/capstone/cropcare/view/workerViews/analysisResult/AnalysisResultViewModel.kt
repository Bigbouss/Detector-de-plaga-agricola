package com.capstone.cropcare.view.workerViews.analysisResult

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject



//@HiltViewModel
//class AnalysisViewModel @Inject constructor(
//    private val context: Application
//) : ViewModel() {
//
//    private val _state = MutableStateFlow<AnalysisState>(AnalysisState.Loading)
//    val state: StateFlow<AnalysisState> = _state
//
//    private val _photoUri = MutableStateFlow<Uri?>(null)
//    val photoUri: StateFlow<Uri?> = _photoUri
//
//    fun setAnalysisResult(result: AnalysisState) {
//        _state.value = result
//    }
//
//    init {
//        // Simulación de analisis
//        viewModelScope.launch {
//            delay(2000)
//            _state.value = AnalysisState.Good //estado
//
//        }
//    }
//
//    // Simulación de análisis photo
//    fun analyzePhoto(bitmap: Bitmap) {
//        viewModelScope.launch {
//            _state.value = AnalysisState.Loading
//            delay(1500) // Simula proceso
//
//
//            _state.value = AnalysisState.Good
//        }
//    }
//}
//sealed class AnalysisState {
//    object Loading : AnalysisState()
//    object Good : AnalysisState()
//    object Bad : AnalysisState()
//    object TryAgain : AnalysisState()
//}
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val context: Application
) : ViewModel() {

    private val _state = MutableStateFlow<AnalysisState>(AnalysisState.Loading)
    val state: StateFlow<AnalysisState> = _state

    // Guarda el bitmap temporalmente para preview
    private val _tempBitmap = MutableStateFlow<Bitmap?>(null)
    val tempBitmap: StateFlow<Bitmap?> = _tempBitmap

    // Path de la imagen guardada permanentemente
    private val _savedImagePath = MutableStateFlow<String?>(null)
    val savedImagePath: StateFlow<String?> = _savedImagePath

    // Analiza la foto capturada
    fun analyzePhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.value = AnalysisState.Loading
            _tempBitmap.value = bitmap // Guarda temporalmente para preview

            delay(1000) // Simula análisis ML

            // Simula resultado (cambia esto según necesites)
            val result = listOf(
                //AnalysisState.Good,
                AnalysisState.Bad,
                //AnalysisState.TryAgain
            ).random()

            _state.value = result

            // Si es Bad, guarda la imagen permanentemente
            if (result is AnalysisState.Bad) {
                saveImagePermanently(bitmap)
            }
        }
    }

    // Guarda la imagen permanentemente solo si es "Bad"
    private suspend fun saveImagePermanently(bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            try {
                val fileName = "crop_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)

                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                _savedImagePath.value = file.absolutePath
                Log.d("AnalysisViewModel", "Imagen guardada en: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e("AnalysisViewModel", "Error guardando imagen", e)
            }
        }
    }

    // Limpia la imagen temporal (llama esto después de enviar el reporte)
    fun clearTemporaryImage() {
        _tempBitmap.value = null
    }

    // Resetea todo para una nueva captura
    fun reset() {
        _state.value = AnalysisState.Loading
        _tempBitmap.value = null
        _savedImagePath.value = null
    }
}

sealed class AnalysisState {
    object Loading : AnalysisState()
    object Good : AnalysisState()
    object Bad : AnalysisState()
    object TryAgain : AnalysisState()
}




