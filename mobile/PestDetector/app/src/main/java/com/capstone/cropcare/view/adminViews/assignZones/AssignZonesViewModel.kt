package com.capstone.cropcare.view.adminViews.assignZones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.usecase.zoneUseCase.GetZonesUseCase
import com.capstone.cropcare.domain.usecase.zoneUseCase.SyncZonesFromBackendUseCase
import com.capstone.cropcare.domain.usecase.workerUseCase.AssignZonesToWorkerUseCase
import com.capstone.cropcare.domain.usecase.workerUseCase.GetWorkerAssignedZonesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignZonesViewModel @Inject constructor(
    private val getZonesUseCase: GetZonesUseCase,
    private val getWorkerAssignedZonesUseCase: GetWorkerAssignedZonesUseCase,
    private val assignZonesToWorkerUseCase: AssignZonesToWorkerUseCase,
    private val syncZonesFromBackendUseCase: SyncZonesFromBackendUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssignZonesState())
    val uiState: StateFlow<AssignZonesState> = _uiState.asStateFlow()

    private var initialZoneIds: Set<String> = emptySet()

    // ✅ CAMBIO: Ahora acepta Int en lugar de String
    fun loadData(workerId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Sincronizar desde backend primero
                val syncResult = syncZonesFromBackendUseCase()

                if (syncResult.isFailure) {
                    android.util.Log.w("AssignZonesVM", "No se pudo sincronizar, usando datos locales")
                }

                // Obtener listas de manera secuencial
                val allZones = getZonesUseCase().first()
                val assignedZones = getWorkerAssignedZonesUseCase(workerId).first()

                val assignedIds = assignedZones.map { it.id }.toSet()
                initialZoneIds = assignedIds

                _uiState.update {
                    it.copy(
                        allZones = allZones,
                        selectedZoneIds = assignedIds,
                        isLoading = false,
                        hasChanges = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar datos"
                    )
                }
            }
        }
    }

    fun toggleZoneSelection(zoneId: String) {
        val currentSelected = _uiState.value.selectedZoneIds
        val newSelected = if (currentSelected.contains(zoneId)) {
            currentSelected - zoneId
        } else {
            currentSelected + zoneId
        }

        _uiState.update {
            it.copy(
                selectedZoneIds = newSelected,
                hasChanges = newSelected != initialZoneIds
            )
        }
    }

    fun saveAssignments(workerId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val zoneIdsStr = _uiState.value.selectedZoneIds.toList()

            if (zoneIdsStr.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Debes seleccionar al menos una zona antes de guardar"
                    )
                }
                return@launch
            }

            val zoneIdsInt = zoneIdsStr.mapNotNull { it.toIntOrNull() }

            if (zoneIdsInt.size != zoneIdsStr.size) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Algunos IDs de zona son inválidos"
                    )
                }
                return@launch
            }

            assignZonesToWorkerUseCase(workerId, zoneIdsInt).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            hasChanges = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error.message ?: "Error al guardar"
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}

data class AssignZonesState(
    val allZones: List<ZoneModel> = emptyList(),
    val selectedZoneIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val hasChanges: Boolean = false,
    val error: String? = null
)