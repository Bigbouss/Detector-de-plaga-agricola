package com.capstone.cropcare.view.workerViews.scanning

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.capstone.cropcare.data.worker.SyncScanWorker
import com.capstone.cropcare.domain.model.ScanResultModel
import com.capstone.cropcare.domain.model.ScanSessionModel
import com.capstone.cropcare.domain.repository.ScanResultRepository
import com.capstone.cropcare.domain.repository.ScanSessionRepository
import com.capstone.cropcare.domain.usecase.SyncScanDataUseCase
import com.capstone.cropcare.ml.PlantClassifier
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ScanningViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: ScanSessionRepository,
    private val scanResultRepository: ScanResultRepository,
    private val syncScanDataUseCase: SyncScanDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanningState())
    val uiState: StateFlow<ScanningState> = _uiState.asStateFlow()

    private val _currentSession = MutableStateFlow<ScanSessionModel?>(null)
    val currentSession: StateFlow<ScanSessionModel?> = _currentSession.asStateFlow()

    private var classifier: PlantClassifier? = null
    private val workManager = WorkManager.getInstance(context)

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.observeSession(sessionId).collect { session ->
                _currentSession.value = session

                // Inicializar clasificador con el tipo de cultivo correcto
                session?.let {
                    if (classifier == null) {
                        classifier = PlantClassifier(context, it.cropName)
                        Log.d("ScanningVM", "🤖 Clasificador inicializado para: ${it.cropName}")
                    }
                }

                Log.d("ScanningVM", "📊 Sesión cargada: ${session?.id}")
            }
        }
    }

    /**
     * Analiza una foto capturada con el modelo ML
     */
    fun analyzePhoto(bitmap: Bitmap) {
        val session = _currentSession.value
        if (session == null) {
            _uiState.update { it.copy(error = "No hay sesión activa") }
            return
        }

        if (classifier == null) {
            _uiState.update { it.copy(error = "Clasificador no inicializado") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }

            try {
                //Guardar foto en almacenamiento
                val photoPath = withContext(Dispatchers.IO) {
                    saveBitmapToFile(bitmap)
                }

                //Clasificar con modelo ML
                val classificationResult = withContext(Dispatchers.Default) {
                    classifier!!.classify(bitmap)
                }

                //Guardar resultado del escaneo
                val scanResult = ScanResultModel(
                    sessionId = session.id,
                    photoPath = photoPath,
                    classification = classificationResult.label,
                    confidence = classificationResult.confidence,
                    hasPlague = classificationResult.isPlague
                )

                scanResultRepository.saveScanResult(scanResult)

                //Actualizar contadores en la sesión
                if (scanResult.hasPlague) {
                    sessionRepository.incrementPlagueCount(session.id)
                } else {
                    sessionRepository.incrementHealthyCount(session.id)
                }

                //Actualizar UI
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        lastClassification = classificationResult.label,
                        confidence = classificationResult.confidence,
                        hasPlague = classificationResult.isPlague,
                        lastScanResultId = scanResult.id
                    )
                }

                Log.d("ScanningVM", "Análisis completado: ${classificationResult.label} (${(classificationResult.confidence * 100).toInt()}%)")

                //Intentar sincronizar en segundo plano
                trySyncInBackground()

            } catch (e: Exception) {
                Log.e("ScanningVM", "Error en análisis", e)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = "Error al procesar imagen: ${e.message}"
                    )
                }
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val timestamp = System.currentTimeMillis()
        val fileName = "plant_scan_$timestamp.jpg"

        // Crear directorio si no existe
        val directory = File(context.filesDir, "plant_scans")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, fileName)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        Log.d("ScanningVM", "Foto guardada: ${file.absolutePath}")
        return file.absolutePath
    }

    fun finishSession() {
        val session = _currentSession.value ?: return

        viewModelScope.launch {
            try {
                sessionRepository.finishSession(session.id)
                Log.d("ScanningVM", "Sesión finalizada: ${session.id}")

                // Sincronizar inmediatamente al finalizar sesión
                syncNow()
            } catch (e: Exception) {
                Log.e("ScanningVM", "Error finalizando sesión", e)
                _uiState.update { it.copy(error = "Error al finalizar sesión") }
            }
        }
    }

    fun cancelSession() {
        val session = _currentSession.value ?: return

        viewModelScope.launch {
            try {
                sessionRepository.cancelSession(session.id)
                Log.d("ScanningVM", "Sesión cancelada: ${session.id}")
            } catch (e: Exception) {
                Log.e("ScanningVM", "Error cancelando sesión", e)
            }
        }
    }

    private fun trySyncInBackground() {
        viewModelScope.launch {
            try {
                val hasPending = syncScanDataUseCase.hasPendingSync()
                if (hasPending) {
                    // Programar sincronización en segundo plano
                    SyncScanWorker.schedule(workManager)
                }
            } catch (e: Exception) {
                Log.w("ScanningVM", "No se pudo verificar sincronización", e)
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSyncing = true) }

                val result = syncScanDataUseCase()

                if (result.isSuccess) {
                    Log.d("ScanningVM", "Sincronización exitosa")
                    _uiState.update { it.copy(isSyncing = false, lastSyncSuccess = true) }
                } else {
                    Log.w("ScanningVM", "Sincronización falló")
                    _uiState.update { it.copy(isSyncing = false, lastSyncSuccess = false) }
                }
            } catch (e: Exception) {
                Log.e("ScanningVM", "Error en sincronización", e)
                _uiState.update { it.copy(isSyncing = false, lastSyncSuccess = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Liberar recursos del clasificador
        classifier?.close()
        classifier = null
    }
}

data class ScanningState(
    val isProcessing: Boolean = false,
    val isSyncing: Boolean = false,
    val lastClassification: String? = null,
    val confidence: Float? = null,
    val hasPlague: Boolean = false,
    val lastScanResultId: String? = null,
    val lastSyncSuccess: Boolean? = null,
    val error: String? = null
)