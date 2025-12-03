package com.capstone.cropcare.view.adminViews.metricsManagement

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.data.remote.api.ReportApiService
import com.capstone.cropcare.data.remote.dto.MetricsResponseDTO
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
class MetricsManagementViewModel @Inject constructor(
    private val reportApiService: ReportApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetricsManagementState())
    val uiState: StateFlow<MetricsManagementState> = _uiState.asStateFlow()

    init {
        loadMetrics()
    }

    fun loadMetrics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val response = reportApiService.getMetrics(
                    period = _uiState.value.period,
                    dateFrom = _uiState.value.dateFrom,
                    dateTo = _uiState.value.dateTo,
                    cultivoId = _uiState.value.cultivoId,
                    zonaId = _uiState.value.zonaId
                )

                if (response.isSuccessful) {
                    val metrics = response.body()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            metrics = metrics
                        )
                    }
                    Log.d("MetricsVM", "Métricas cargadas exitosamente")
                } else {
                    throw Exception("Error ${response.code()}: ${response.message()}")
                }

            } catch (e: Exception) {
                Log.e("MetricsVM", "Error cargando métricas", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar métricas: ${e.message}"
                    )
                }
            }
        }
    }

    fun setPeriod(period: String) {
        _uiState.update { it.copy(period = period) }
        loadMetrics()
    }

    fun exportPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExportingPdf = true) }

            try {
                val response = reportApiService.exportPdf(
                    period = _uiState.value.period,
                    dateFrom = _uiState.value.dateFrom,
                    dateTo = _uiState.value.dateTo
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val fileName = "metricas_${System.currentTimeMillis()}.pdf"

                        withContext(Dispatchers.IO) {
                            savePdfFile(body.bytes(), fileName)
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "PDF descargado en Descargas/$fileName",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        Log.d("MetricsVM", "PDF guardado: $fileName")
                    }
                } else {
                    throw Exception("Error ${response.code()}: ${response.message()}")
                }

            } catch (e: Exception) {
                Log.e("MetricsVM", "Error exportando PDF", e)
                _uiState.update {
                    it.copy(error = "Error al exportar PDF: ${e.message}")
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error al descargar PDF: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                _uiState.update { it.copy(isExportingPdf = false) }
            }
        }
    }

    private fun savePdfFile(pdfBytes: ByteArray, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ (API 29+) - Scoped Storage
            savePdfToMediaStore(pdfBytes, fileName)
        } else {
            // Android 9 y anteriores (API 24-28)
            savePdfToExternalStorage(pdfBytes, fileName)
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
    private fun savePdfToMediaStore(pdfBytes: ByteArray, fileName: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )

        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(pdfBytes)
                outputStream.flush()
            }
        } ?: throw Exception("No se pudo crear el archivo en MediaStore")
    }

    @Suppress("DEPRECATION")
    private fun savePdfToExternalStorage(pdfBytes: ByteArray, fileName: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )

        // Crear el directorio si no existe
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val file = File(downloadsDir, fileName)

        FileOutputStream(file).use { outputStream ->
            outputStream.write(pdfBytes)
            outputStream.flush()
        }

        // Notificar al sistema que hay un nuevo archivo
        android.media.MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            arrayOf("application/pdf"),
            null
        )
    }
}

data class MetricsManagementState(
    val isLoading: Boolean = false,
    val isExportingPdf: Boolean = false,
    val metrics: MetricsResponseDTO? = null,
    val period: String = "month",
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val cultivoId: Int? = null,
    val zonaId: Int? = null,
    val error: String? = null
)