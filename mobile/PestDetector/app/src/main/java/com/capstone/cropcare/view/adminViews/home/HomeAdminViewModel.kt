package com.capstone.cropcare.view.adminViews.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.WorkerModel
import com.capstone.cropcare.domain.usecase.workerUseCase.DeleteWorkerUseCase
import com.capstone.cropcare.domain.usecase.workerUseCase.GetWorkersUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeAdminViewModel @Inject constructor(
    private val getWorkersUseCase: GetWorkersUseCase,
    private val deleteWorkerUseCase: DeleteWorkerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeAdminState())
    val uiState: StateFlow<HomeAdminState> = _uiState.asStateFlow()

    init {
        loadWorkers()
    }

    fun loadWorkers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getWorkersUseCase().collect { workers ->
                    _uiState.update {
                        it.copy(
                            workers = workers,
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

    fun showDeleteDialog(worker: WorkerModel) {
        _uiState.update {
            it.copy(
                showDeleteDialog = true,
                workerToDelete = worker
            )
        }
    }

    fun hideDeleteDialog() {
        _uiState.update {
            it.copy(
                showDeleteDialog = false,
                workerToDelete = null
            )
        }
    }

    fun deleteWorker() {
        val worker = _uiState.value.workerToDelete ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }

            val result = deleteWorkerUseCase(worker.id)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            showDeleteDialog = false,
                            workerToDelete = null
                        )
                    }
                    loadWorkers()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            error = exception.message ?: "Error al eliminar trabajador"
                        )
                    }
                }
            )
        }
    }

    fun showOptionsMenu(worker: WorkerModel) {
        _uiState.update {
            it.copy(
                showOptionsMenu = true,
                selectedWorker = worker
            )
        }
    }

    fun hideOptionsMenu() {
        _uiState.update {
            it.copy(
                showOptionsMenu = false,
                selectedWorker = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class HomeAdminState(
    val workers: List<WorkerModel> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showOptionsMenu: Boolean = false,
    val workerToDelete: WorkerModel? = null,
    val selectedWorker: WorkerModel? = null,
    val error: String? = null
)