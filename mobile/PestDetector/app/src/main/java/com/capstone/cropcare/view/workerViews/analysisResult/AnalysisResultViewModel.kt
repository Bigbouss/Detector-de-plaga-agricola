package com.capstone.cropcare.view.workerViews.analysisResult

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.ml.PlantClassifier
//import com.capstone.cropcare.ml.PlantDiseaseClassifier
//import com.capstone.cropcare.ml.PlantClassifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    private val _tempBitmap = MutableStateFlow<Bitmap?>(null)
    val tempBitmap: StateFlow<Bitmap?> = _tempBitmap

    private val _savedImagePath = MutableStateFlow<String?>(null)
    val savedImagePath: StateFlow<String?> = _savedImagePath

    private val _diseaseInfo = MutableStateFlow<DiseaseInfo?>(null)
    val diseaseInfo: StateFlow<DiseaseInfo?> = _diseaseInfo

    // Se inicializa después con el tipo de cultivo
    private lateinit var classifier: PlantClassifier

    fun initClassifier(cropType: String) {
        classifier = PlantClassifier(context, cropType)
    }

    fun analyzePhoto(bitmap: Bitmap) {
        if (!::classifier.isInitialized) {
            Log.e("AnalysisVM", "Classifier no inicializado: llama a initClassifier() antes")
            _state.value = AnalysisState.TryAgain
            return
        }

        viewModelScope.launch {
            try {
                _state.value = AnalysisState.Loading
                _tempBitmap.value = bitmap

                val result = withContext(Dispatchers.Default) {
                    classifier.classify(bitmap)
                }

                Log.d("AnalysisVM", "Pred: ${result.label} conf=${result.confidence}")

                val parsed = parseLabel(result.label, result.confidence)
                _diseaseInfo.value = parsed

                val lowConfidence = result.confidence < 0.60f
                val isHealthy = result.label.contains("Healthy", ignoreCase = true)

                _state.value = when {
                    lowConfidence -> AnalysisState.TryAgain
                    isHealthy -> AnalysisState.Good
                    else -> {
                        saveImagePermanently(bitmap)
                        AnalysisState.Bad
                    }
                }

            } catch (e: Exception) {
                Log.e("AnalysisVM", "Error analizando", e)
                _state.value = AnalysisState.TryAgain
            }
        }
    }

    private fun parseLabel(label: String, conf: Float): DiseaseInfo {
        // Tus etiquetas son "Apple_Black_Rot", etc → separar por "_"
        val parts = label.split("_")
        val plantType = parts.getOrNull(0) ?: "Desconocido"
        val diseaseName = label.replace("${plantType}_", "").replace("_", " ")

        val isHealthy = label.contains("Healthy", ignoreCase = true)

        return DiseaseInfo(
            plantType = plantType,
            diseaseName = diseaseName,
            confidence = conf,
            isHealthy = isHealthy,
            rawLabel = label
        )
    }

    private suspend fun saveImagePermanently(bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, "crop_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            _savedImagePath.value = file.absolutePath
        }
    }

    fun clearTemporaryImage() {
        _tempBitmap.value = null
    }

    fun reset() {
        _state.value = AnalysisState.Loading
        _tempBitmap.value = null
        _savedImagePath.value = null
        _diseaseInfo.value = null
    }

    override fun onCleared() {
        if (::classifier.isInitialized) {
            classifier.close()
        }
        super.onCleared()
    }
}

sealed class AnalysisState {
    object Loading : AnalysisState()
    object Good : AnalysisState()
    object Bad : AnalysisState()
    object TryAgain : AnalysisState()
}

data class DiseaseInfo(
    val plantType: String,
    val diseaseName: String,
    val confidence: Float,
    val isHealthy: Boolean,
    val rawLabel: String
)




