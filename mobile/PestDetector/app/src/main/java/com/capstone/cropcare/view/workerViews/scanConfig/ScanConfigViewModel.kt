package com.capstone.cropcare.view.workerViews.scanConfig

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.model.SessionStatus
import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.AuthRepository
import com.capstone.cropcare.domain.repository.CropZoneRepository
import com.capstone.cropcare.domain.repository.ScanSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanConfigViewModel @Inject constructor(
    private val cropZoneRepository: CropZoneRepository,
    private val scanSessionRepository: ScanSessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanConfigState())
    val uiState: StateFlow<ScanConfigState> = _uiState.asStateFlow()

    // Zonas disponibles (asignadas al worker)
    private val _availableZones = MutableStateFlow<List<ZoneModel>>(emptyList())
    val availableZones: StateFlow<List<ZoneModel>> = _availableZones.asStateFlow()

    // Cultivos de la zona seleccionada
    private val _availableCrops = MutableStateFlow<List<CropModel>>(emptyList())
    val availableCrops: StateFlow<List<CropModel>> = _availableCrops.asStateFlow()

    init {
        loadAssignedZones()
    }

    private fun loadAssignedZones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                Log.d("ScanConfigVM", "Iniciando carga de zonas...")

                // Sincronizar zonas desde backend
                val syncResult = cropZoneRepository.syncAssignedZonesFromBackend()

                syncResult.fold(
                    onSuccess = {
                        Log.d("ScanConfigVM", "Sincronización exitosa")
                    },
                    onFailure = { error ->
                        Log.w("ScanConfigVM", "Error sincronizando: ${error.message}")
                        _uiState.update {
                            it.copy(error = "No se pudo sincronizar: ${error.message}")
                        }
                    }
                )

                // Cargar desde Room (cache local)
                cropZoneRepository.getAllZones().collect { zones ->
                    _availableZones.value = zones
                    Log.d("ScanConfigVM", "${zones.size} zonas cargadas: ${zones.map { it.name }}")

                    if (zones.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "No tienes zonas asignadas. Contacta al administrador."
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = null) }
                    }
                }

            } catch (e: Exception) {
                Log.e("ScanConfigVM", "Error cargando zonas", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectZone(zone: ZoneModel) {
        _uiState.update { it.copy(selectedZone = zone, selectedCrop = null) }
        _availableCrops.value = emptyList()

        viewModelScope.launch {
            try {
                // Sincronizar cultivos desde backend
                val syncResult = cropZoneRepository.syncCropsForZone(zone.id)

                if (syncResult.isSuccess) {
                    Log.d("ScanConfigVM", "Cultivos sincronizados para zona ${zone.name}")
                }

                // Cargar desde Room
                cropZoneRepository.getCropsByZone(zone.id).collect { crops ->
                    _availableCrops.value = crops
                    Log.d("ScanConfigVM", "Cultivos cargados: ${crops.map { it.name }}")
                }

            } catch (e: Exception) {
                Log.e("ScanConfigVM", "Error cargando cultivos", e)
                _uiState.update {
                    it.copy(error = "Error al cargar cultivos: ${e.message}")
                }
            }
        }
    }

    fun selectCrop(crop: CropModel) {
        _uiState.update { it.copy(selectedCrop = crop) }
    }
    fun startSession(onSuccess: (String) -> Unit) {
        val zone = _uiState.value.selectedZone
        val crop = _uiState.value.selectedCrop

        if (zone == null || crop == null) {
            _uiState.update { it.copy(error = "Selecciona zona y cultivo") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingSession = true, error = null) }

            try {
                val currentUser = authRepository.getCurrentUser()
                    ?: throw Exception("Usuario no autenticado")

                val displayName = currentUser.email.substringBefore("@")

                // Crear nueva sesión
                val session = com.capstone.cropcare.domain.model.ScanSessionModel(
                    workerId = currentUser.id.toString(),
                    workerName = displayName,
                    zoneId = zone.id,
                    zoneName = zone.name,
                    cropId = crop.id,
                    cropName = crop.name,
                    status = SessionStatus.ACTIVE,
                    modelVersion = getModelVersionForCrop(crop.name)
                )

                val result = scanSessionRepository.createSession(session)

                result.fold(
                    onSuccess = { createdSession ->
                        Log.d("ScanConfigVM", "Sesión creada: ${createdSession.id}")
                        _uiState.update { it.copy(isCreatingSession = false) }
                        onSuccess(createdSession.id)
                    },
                    onFailure = { error ->
                        Log.e("ScanConfigVM", "Error creando sesión", error)
                        _uiState.update {
                            it.copy(
                                isCreatingSession = false,
                                error = "Error al iniciar sesión: ${error.message}"
                            )
                        }
                    }
                )

            } catch (e: Exception) {
                Log.e("ScanConfigVM", "Exception al crear sesión", e)
                _uiState.update {
                    it.copy(
                        isCreatingSession = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun getModelVersionForCrop(cropName: String): String {
        return when (cropName.lowercase()) {
            "manzanas", "apple" -> "apple_v1.0"
            "tomates", "tomato" -> "tomato_v1.0"
            "papas", "potato" -> "potato_v1.0"
            else -> "generic_v1.0"
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ScanConfigState(
    val selectedZone: ZoneModel? = null,
    val selectedCrop: CropModel? = null,
    val isLoading: Boolean = false,
    val isCreatingSession: Boolean = false,
    val error: String? = null
)