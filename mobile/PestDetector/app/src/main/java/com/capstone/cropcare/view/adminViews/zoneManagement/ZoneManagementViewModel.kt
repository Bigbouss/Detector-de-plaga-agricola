package com.capstone.cropcare.view.adminViews.zoneManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.usecase.*
import com.capstone.cropcare.domain.usecase.cropUseCase.AddCropToZoneUseCase
import com.capstone.cropcare.domain.usecase.cropUseCase.DeleteCropUseCase
import com.capstone.cropcare.domain.usecase.cropUseCase.GetCropsByZoneUseCase
import com.capstone.cropcare.domain.usecase.zoneUseCase.CreateZoneUseCase
import com.capstone.cropcare.domain.usecase.zoneUseCase.DeleteZoneUseCase
import com.capstone.cropcare.domain.usecase.zoneUseCase.GetZonesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZoneManagementViewModel @Inject constructor(
    private val getZonesUseCase: GetZonesUseCase,
    private val createZoneUseCase: CreateZoneUseCase,
    private val deleteZoneUseCase: DeleteZoneUseCase,
    private val getCropsByZoneUseCase: GetCropsByZoneUseCase,
    private val addCropToZoneUseCase: AddCropToZoneUseCase,
    private val deleteCropUseCase: DeleteCropUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ZoneManagementState())
    val uiState: StateFlow<ZoneManagementState> = _uiState.asStateFlow()

    init {
        loadZones()
    }

    private fun loadZones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getZonesUseCase().collect { zones ->
                    _uiState.update {
                        it.copy(
                            zones = zones,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun selectZone(zone: ZoneModel) {
        _uiState.update { it.copy(selectedZone = zone) }
        loadCropsForZone(zone.id)
    }

    private fun loadCropsForZone(zoneId: String) {
        viewModelScope.launch {
            try {
                getCropsByZoneUseCase(zoneId).collect { crops ->
                    _uiState.update { it.copy(cropsInSelectedZone = crops) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun showCreateZoneDialog() {
        _uiState.update {
            it.copy(
                showCreateZoneDialog = true,
                newZoneName = "",
                newZoneDescription = ""
            )
        }
    }

    fun hideCreateZoneDialog() {
        _uiState.update {
            it.copy(
                showCreateZoneDialog = false,
                newZoneName = "",
                newZoneDescription = ""
            )
        }
    }

    fun onNewZoneNameChanged(name: String) {
        _uiState.update { it.copy(newZoneName = name) }
    }

    fun onNewZoneDescriptionChanged(description: String) {
        _uiState.update { it.copy(newZoneDescription = description) }
    }

    fun createZone() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingZone = true, error = null) }

            val result = createZoneUseCase(
                name = _uiState.value.newZoneName,
                description = _uiState.value.newZoneDescription.takeIf { it.isNotBlank() }
            )

            result.fold(
                onSuccess = { zone ->
                    _uiState.update {
                        it.copy(
                            isCreatingZone = false,
                            showCreateZoneDialog = false,
                            newZoneName = "",
                            newZoneDescription = ""
                        )
                    }
                    loadZones()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isCreatingZone = false,
                            error = exception.message ?: "Error al crear zona"
                        )
                    }
                }
            )
        }
    }

    fun showDeleteZoneDialog(zone: ZoneModel) {
        _uiState.update {
            it.copy(
                showDeleteZoneDialog = true,
                zoneToDelete = zone
            )
        }
    }

    fun hideDeleteZoneDialog() {
        _uiState.update {
            it.copy(
                showDeleteZoneDialog = false,
                zoneToDelete = null
            )
        }
    }

    fun deleteZone() {
        val zone = _uiState.value.zoneToDelete ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingZone = true) }

            val result = deleteZoneUseCase(zone)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeletingZone = false,
                            showDeleteZoneDialog = false,
                            zoneToDelete = null,
                            selectedZone = null,
                            cropsInSelectedZone = emptyList()
                        )
                    }
                    loadZones()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isDeletingZone = false,
                            error = exception.message ?: "Error al eliminar zona"
                        )
                    }
                }
            )
        }
    }

    fun showAddCropDialog() {
        _uiState.update {
            it.copy(
                showAddCropDialog = true,
                newCropName = ""
            )
        }
    }

    fun hideAddCropDialog() {
        _uiState.update {
            it.copy(
                showAddCropDialog = false,
                newCropName = ""
            )
        }
    }

    fun onNewCropNameChanged(name: String) {
        _uiState.update { it.copy(newCropName = name) }
    }

    fun addCropToZone() {
        val zone = _uiState.value.selectedZone ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingCrop = true, error = null) }

            val result = addCropToZoneUseCase(
                cropName = _uiState.value.newCropName,
                zoneId = zone.id
            )

            result.fold(
                onSuccess = { crop ->
                    _uiState.update {
                        it.copy(
                            isAddingCrop = false,
                            showAddCropDialog = false,
                            newCropName = ""
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isAddingCrop = false,
                            error = exception.message ?: "Error al agregar cultivo"
                        )
                    }
                }
            )
        }
    }

    fun showDeleteCropDialog(crop: CropModel) {
        _uiState.update {
            it.copy(
                showDeleteCropDialog = true,
                cropToDelete = crop
            )
        }
    }

    fun hideDeleteCropDialog() {
        _uiState.update {
            it.copy(
                showDeleteCropDialog = false,
                cropToDelete = null
            )
        }
    }

    fun deleteCrop() {
        val crop = _uiState.value.cropToDelete ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingCrop = true) }

            val result = deleteCropUseCase(crop)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeletingCrop = false,
                            showDeleteCropDialog = false,
                            cropToDelete = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isDeletingCrop = false,
                            error = exception.message ?: "Error al eliminar cultivo"
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ZoneManagementState(
    val zones: List<ZoneModel> = emptyList(),
    val selectedZone: ZoneModel? = null,
    val cropsInSelectedZone: List<CropModel> = emptyList(),

    // Loading states
    val isLoading: Boolean = false,
    val isCreatingZone: Boolean = false,
    val isDeletingZone: Boolean = false,
    val isAddingCrop: Boolean = false,
    val isDeletingCrop: Boolean = false,

    // Dialog states
    val showCreateZoneDialog: Boolean = false,
    val showDeleteZoneDialog: Boolean = false,
    val showAddCropDialog: Boolean = false,
    val showDeleteCropDialog: Boolean = false,

    // Form data
    val newZoneName: String = "",
    val newZoneDescription: String = "",
    val newCropName: String = "",

    // Delete confirmation
    val zoneToDelete: ZoneModel? = null,
    val cropToDelete: CropModel? = null,

    val error: String? = null
)