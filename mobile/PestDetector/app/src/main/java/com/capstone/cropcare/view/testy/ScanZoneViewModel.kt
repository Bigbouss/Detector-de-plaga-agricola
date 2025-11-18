package com.capstone.cropcare.view.testy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.AuthRepository
import com.capstone.cropcare.domain.usecase.workerUseCase.GetWorkerAssignedZonesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanZoneViewModel @Inject constructor(
    private val getWorkerAssignedZonesUseCase: GetWorkerAssignedZonesUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _zones = MutableStateFlow<List<ZoneModel>>(emptyList())
    val zones: StateFlow<List<ZoneModel>> = _zones

    init {
        loadZones()
    }

    private fun loadZones() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            getWorkerAssignedZonesUseCase(user.uid).collect { zoneList ->
                _zones.value = zoneList
            }
        }
    }
}
