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



//
//package com.capstone.cropcare.view.workerViews.analysisResult
//
//import android.app.Application
//import android.graphics.Bitmap
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.capstone.cropcare.ml.PlantDiseaseClassifier
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File
//import java.io.FileOutputStream
//import javax.inject.Inject
//
//@HiltViewModel
//class AnalysisViewModel @Inject constructor(
//    private val context: Application
//) : ViewModel() {
//
//    private val _state = MutableStateFlow<AnalysisState>(AnalysisState.Loading)
//    val state: StateFlow<AnalysisState> = _state
//
//    private val _tempBitmap = MutableStateFlow<Bitmap?>(null)
//    val tempBitmap: StateFlow<Bitmap?> = _tempBitmap
//
//    private val _savedImagePath = MutableStateFlow<String?>(null)
//    val savedImagePath: StateFlow<String?> = _savedImagePath
//
//    // Información detallada del resultado
//    private val _diseaseInfo = MutableStateFlow<DiseaseInfo?>(null)
//    val diseaseInfo: StateFlow<DiseaseInfo?> = _diseaseInfo
//
//    private var classifier: PlantDiseaseClassifier? = null
//
//    init {
//        classifier = PlantDiseaseClassifier(context)
//    }
//
//    fun analyzePhoto(bitmap: Bitmap) {
//        viewModelScope.launch {
//            _state.value = AnalysisState.Loading
//            _tempBitmap.value = bitmap
//
//            val result = withContext(Dispatchers.Default) {
//                classifier?.classify(bitmap)
//            }
//
//            if (result != null) {
//                Log.d("AnalysisViewModel", "Resultado: ${result.label} - Confianza: ${result.confidence}")
//
//                // Parsear el resultado
//                val diseaseInfo = parseClassification(result.label, result.confidence)
//                _diseaseInfo.value = diseaseInfo
//
//                // Determinar estado según el resultado
//                _state.value = when {
//                    // Confianza muy baja - pedir otra foto
//                    result.confidence < 0.60f -> {
//                        Log.d("AnalysisViewModel", "Confianza baja (${result.confidence}), TryAgain")
//                        AnalysisState.TryAgain
//                    }
//
//                    // Unknown - no se reconoce la planta
//                    result.label.equals("Unknown", ignoreCase = true) -> {
//                        Log.d("AnalysisViewModel", "Planta no reconocida, TryAgain")
//                        AnalysisState.TryAgain
//                    }
//
//                    // Planta saludable
//                    result.label.contains("healthy", ignoreCase = true) -> {
//                        Log.d("AnalysisViewModel", "Planta saludable: ${diseaseInfo.plantType}")
//                        AnalysisState.Good
//                    }
//
//                    // Enfermedad detectada
//                    else -> {
//                        Log.d("AnalysisViewModel", "Enfermedad: ${diseaseInfo.diseaseName}")
//                        saveImagePermanently(bitmap)
//                        AnalysisState.Bad
//                    }
//                }
//            } else {
//                Log.e("AnalysisViewModel", "Error en clasificación")
//                _state.value = AnalysisState.TryAgain
//            }
//        }
//    }
//
//    private fun parseClassification(label: String, confidence: Float): DiseaseInfo {
//        // Parsear el formato: "PlantType___DiseaseName"
//        val parts = label.split("___")
//
//        return when {
//            parts.size >= 2 -> {
//                val plantType = formatPlantType(parts[0])
//                val diseaseName = formatDiseaseName(parts[1])
//
//                DiseaseInfo(
//                    plantType = plantType,
//                    diseaseName = diseaseName,
//                    confidence = confidence,
//                    isHealthy = diseaseName.equals("Saludable", ignoreCase = true),
//                    rawLabel = label
//                )
//            }
//            label.equals("Unknown", ignoreCase = true) -> {
//                DiseaseInfo(
//                    plantType = "Desconocido",
//                    diseaseName = "No reconocido",
//                    confidence = confidence,
//                    isHealthy = false,
//                    rawLabel = label
//                )
//            }
//            else -> {
//                DiseaseInfo(
//                    plantType = "Desconocido",
//                    diseaseName = label,
//                    confidence = confidence,
//                    isHealthy = false,
//                    rawLabel = label
//                )
//            }
//        }
//    }
//
//    private fun formatPlantType(plantType: String): String {
//        return when {
//            plantType.contains("Apple", ignoreCase = true) -> "Manzana"
//            plantType.contains("Corn", ignoreCase = true) ||
//                    plantType.contains("maize", ignoreCase = true) -> "Maíz"
//            plantType.contains("Potato", ignoreCase = true) -> "Papa"
//            else -> plantType.replace("_", " ").trim()
//        }
//    }
//
//    private fun formatDiseaseName(diseaseName: String): String {
//        return when {
//            diseaseName.equals("healthy", ignoreCase = true) -> "Saludable"
//            diseaseName.contains("scab", ignoreCase = true) -> "Sarna del manzano"
//            diseaseName.contains("Black_rot", ignoreCase = true) -> "Pudrición negra"
//            diseaseName.contains("Cedar_apple_rust", ignoreCase = true) -> "Roya del cedro"
//            diseaseName.contains("Cercospora", ignoreCase = true) -> "Mancha gris de la hoja"
//            diseaseName.contains("Common_rust", ignoreCase = true) -> "Roya común"
//            diseaseName.contains("Northern_Leaf_Blight", ignoreCase = true) -> "Tizón norteño"
//            diseaseName.contains("Early_blight", ignoreCase = true) -> "Tizón temprano"
//            diseaseName.contains("Late_blight", ignoreCase = true) -> "Tizón tardío"
//            else -> diseaseName.replace("_", " ").trim()
//        }
//    }
//
//    private suspend fun saveImagePermanently(bitmap: Bitmap) {
//        withContext(Dispatchers.IO) {
//            try {
//                val fileName = "crop_${System.currentTimeMillis()}.jpg"
//                val file = File(context.filesDir, fileName)
//
//                FileOutputStream(file).use { out ->
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
//                }
//
//                _savedImagePath.value = file.absolutePath
//                Log.d("AnalysisViewModel", "Imagen guardada en: ${file.absolutePath}")
//            } catch (e: Exception) {
//                Log.e("AnalysisViewModel", "Error guardando imagen", e)
//            }
//        }
//    }
//
//    fun clearTemporaryImage() {
//        _tempBitmap.value = null
//    }
//
//    fun reset() {
//        _state.value = AnalysisState.Loading
//        _tempBitmap.value = null
//        _savedImagePath.value = null
//        _diseaseInfo.value = null
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        classifier?.close()
//    }
//}
//
//// Data class para información estructurada
//data class DiseaseInfo(
//    val plantType: String,
//    val diseaseName: String,
//    val confidence: Float,
//    val isHealthy: Boolean,
//    val rawLabel: String
//)
//
//sealed class AnalysisState {
//    object Loading : AnalysisState()
//    object Good : AnalysisState()
//    object Bad : AnalysisState()
//    object TryAgain : AnalysisState()
//}