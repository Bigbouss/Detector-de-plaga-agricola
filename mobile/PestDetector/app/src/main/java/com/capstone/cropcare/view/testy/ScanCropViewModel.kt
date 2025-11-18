package com.capstone.cropcare.view.testy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanCropViewModel @Inject constructor(
    private val cropZoneRepository: CropZoneRepository
) : ViewModel() {

    private val _crops = MutableStateFlow<List<CropModel>>(emptyList())
    val crops: StateFlow<List<CropModel>> = _crops

    fun loadCrops(zoneId: String) {
        viewModelScope.launch {
            cropZoneRepository.getCropsByZone(zoneId).collect { cropList ->
                _crops.value = cropList
            }
        }
    }
}
