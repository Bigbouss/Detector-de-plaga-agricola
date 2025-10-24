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

    // 👇 NUEVO: Mapa de cultivos por zona para evitar recargas innecesarias
    private val cropsCache = mutableMapOf<String, List<CropModel>>()

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

                    // Recargar cultivos de todas las zonas
                    zones.forEach { zone ->
                        loadCropsForZone(zone.id)
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

    // 👇 NUEVO: Toggle expandir/colapsar zona
    fun toggleZoneExpansion(zoneId: String) {
        val currentExpanded = _uiState.value.expandedZoneIds.toMutableSet()

        if (currentExpanded.contains(zoneId)) {
            currentExpanded.remove(zoneId)
        } else {
            currentExpanded.add(zoneId)
            // Cargar cultivos si no están en cache
            if (!cropsCache.containsKey(zoneId)) {
                loadCropsForZone(zoneId)
            }
        }

        _uiState.update { it.copy(expandedZoneIds = currentExpanded) }
    }

    private fun loadCropsForZone(zoneId: String) {
        viewModelScope.launch {
            try {
                getCropsByZoneUseCase(zoneId).collect { crops ->
                    cropsCache[zoneId] = crops

                    // Actualizar el mapa de cultivos
                    val updatedCropsMap = _uiState.value.cropsPerZone.toMutableMap()
                    updatedCropsMap[zoneId] = crops
                    _uiState.update { it.copy(cropsPerZone = updatedCropsMap) }
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
                            newZoneDescription = "",
                            expandedZoneIds = setOf(zone.id) // 👈 Auto-expandir zona recién creada
                        )
                    }
                    // No necesitamos loadZones() porque el Flow se actualiza automáticamente
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

    fun showZoneOptionsMenu(zone: ZoneModel) {
        _uiState.update { it.copy(selectedZoneForMenu = zone) }
    }

    fun hideZoneOptionsMenu() {
        _uiState.update { it.copy(selectedZoneForMenu = null) }
    }

    fun showDeleteZoneDialog(zone: ZoneModel) {
        _uiState.update {
            it.copy(
                showDeleteZoneDialog = true,
                zoneToDelete = zone,
                selectedZoneForMenu = null // Cerrar menú
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
                    // Limpiar del cache
                    cropsCache.remove(zone.id)

                    val updatedCropsMap = _uiState.value.cropsPerZone.toMutableMap()
                    updatedCropsMap.remove(zone.id)

                    val updatedExpanded = _uiState.value.expandedZoneIds.toMutableSet()
                    updatedExpanded.remove(zone.id)

                    _uiState.update {
                        it.copy(
                            isDeletingZone = false,
                            showDeleteZoneDialog = false,
                            zoneToDelete = null,
                            cropsPerZone = updatedCropsMap,
                            expandedZoneIds = updatedExpanded
                        )
                    }
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

    fun showAddCropDialog(zone: ZoneModel) {
        _uiState.update {
            it.copy(
                showAddCropDialog = true,
                selectedZoneForCrop = zone,
                newCropName = "",
                selectedZoneForMenu = null // Cerrar menú
            )
        }
    }

    fun hideAddCropDialog() {
        _uiState.update {
            it.copy(
                showAddCropDialog = false,
                selectedZoneForCrop = null,
                newCropName = ""
            )
        }
    }

    fun onNewCropNameChanged(name: String) {
        _uiState.update { it.copy(newCropName = name) }
    }

    fun addCropToZone() {
        val zone = _uiState.value.selectedZoneForCrop ?: return
        val cropName = _uiState.value.newCropName.trim()

        // 👇 Validar duplicados
        val existingCrops = _uiState.value.cropsPerZone[zone.id] ?: emptyList()
        if (existingCrops.any { it.name.equals(cropName, ignoreCase = true) }) {
            _uiState.update {
                it.copy(error = "Ya existe un cultivo con ese nombre en esta zona")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingCrop = true, error = null) }

            val result = addCropToZoneUseCase(
                cropName = cropName,
                zoneId = zone.id
            )

            result.fold(
                onSuccess = { crop ->
                    _uiState.update {
                        it.copy(
                            isAddingCrop = false,
                            showAddCropDialog = false,
                            newCropName = "",
                            selectedZoneForCrop = null,
                            expandedZoneIds = it.expandedZoneIds + zone.id // 👈 Mantener expandida
                        )
                    }
                    // El Flow de Room actualizará automáticamente
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
                    // El Flow actualizará automáticamente
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
    val cropsPerZone: Map<String, List<CropModel>> = emptyMap(), // 👈 Mapa de cultivos por zona
    val expandedZoneIds: Set<String> = emptySet(), // 👈 IDs de zonas expandidas

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

    // Selected items
    val selectedZoneForMenu: ZoneModel? = null, // 👈 Para menú de opciones
    val selectedZoneForCrop: ZoneModel? = null, // 👈 Para agregar cultivo
    val zoneToDelete: ZoneModel? = null,
    val cropToDelete: CropModel? = null,

    val error: String? = null
)