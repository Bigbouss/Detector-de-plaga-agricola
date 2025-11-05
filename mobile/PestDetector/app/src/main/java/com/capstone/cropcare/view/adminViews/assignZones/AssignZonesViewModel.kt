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
    private val GetZonesUseCase: GetZonesUseCase,
    private val GetWorkerAssignedZonesUseCase: GetWorkerAssignedZonesUseCase,
    private val assignZonesToWorkerUseCase: AssignZonesToWorkerUseCase,
    private val syncZonesFromBackendUseCase: SyncZonesFromBackendUseCase // 👈 NUEVO
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssignZonesState())
    val uiState: StateFlow<AssignZonesState> = _uiState.asStateFlow()

    private var initialZoneIds: Set<String> = emptySet()

    fun loadData(workerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // 👇 NUEVO: Sincronizar desde backend primero
                val syncResult = syncZonesFromBackendUseCase()

                if (syncResult.isFailure) {
                    // Si falla la sincronización, aún intentamos cargar datos locales
                    android.util.Log.w("AssignZonesVM", "⚠️ No se pudo sincronizar, usando datos locales")
                }

                // Obtener listas de manera secuencial
                val allZones = GetZonesUseCase().first()
                val assignedZones = GetWorkerAssignedZonesUseCase(workerId).first()

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

    fun saveAssignments(workerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val zoneIds = _uiState.value.selectedZoneIds.toList()

            if (zoneIds.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Debes seleccionar al menos una zona antes de guardar"
                    )
                }
                return@launch
            }

            assignZonesToWorkerUseCase(workerId, zoneIds).fold(
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