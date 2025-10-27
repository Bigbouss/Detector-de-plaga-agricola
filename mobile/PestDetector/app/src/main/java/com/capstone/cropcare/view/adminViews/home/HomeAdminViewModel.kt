//package com.capstone.cropcare.view.adminViews.home
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.capstone.cropcare.domain.model.UserModel
//import com.capstone.cropcare.domain.usecase.workerUseCase.DeleteWorkerUseCase
//import com.capstone.cropcare.domain.usecase.workerUseCase.GetWorkersUseCase
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class HomeAdminViewModel @Inject constructor(
//    private val getWorkersUseCase: GetWorkersUseCase,
//    private val deleteWorkerUseCase: DeleteWorkerUseCase
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(HomeAdminState())
//    val uiState: StateFlow<HomeAdminState> = _uiState.asStateFlow()
//
//    init {
//        loadWorkers()
//    }
//
//    fun loadWorkers() {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//
//            try {
//                getWorkersUseCase().collect { workers ->
//                    _uiState.update {
//                        it.copy(
//                            workers = workers,
//                            isLoading = false,
//                            error = null
//                        )
//                    }
//                }
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(
//                        isLoading = false,
//                        error = e.message ?: "Error al cargar trabajadores"
//                    )
//                }
//            }
//        }
//    }
//
//    fun showDeleteDialog(worker: UserModel) {
//        _uiState.update {
//            it.copy(
//                workerToDelete = worker,
//                showDeleteDialog = true
//            )
//        }
//    }
//
//    fun hideDeleteDialog() {
//        _uiState.update {
//            it.copy(
//                workerToDelete = null,
//                showDeleteDialog = false
//            )
//        }
//    }
//
//    fun deleteWorker() {
//        val worker = _uiState.value.workerToDelete ?: return
//
//        viewModelScope.launch {
//            _uiState.update { it.copy(isDeleting = true) }
//
//            val result = deleteWorkerUseCase(worker.uid)
//
//            result.fold(
//                onSuccess = {
//                    _uiState.update {
//                        it.copy(
//                            isDeleting = false,
//                            showDeleteDialog = false,
//                            workerToDelete = null
//                        )
//                    }
//                    loadWorkers() // Recargar lista
//                },
//                onFailure = { exception ->
//                    _uiState.update {
//                        it.copy(
//                            isDeleting = false,
//                            error = exception.message ?: "Error al eliminar trabajador"
//                        )
//                    }
//                }
//            )
//        }
//    }
//
//    fun clearError() {
//        _uiState.update { it.copy(error = null) }
//    }
//
//    fun showAssignZonesDialog(worker: UserModel) {
//        _uiState.update {
//            it.copy(
//                selectedWorker = worker,
//                showAssignZonesDialog = true
//            )
//        }
//    }
//
//    fun hideAssignZonesDialog() {
//        _uiState.update {
//            it.copy(
//                selectedWorker = null,
//                showAssignZonesDialog = false
//            )
//        }
//    }
//}
//
//data class HomeAdminState(
//    val workers: List<UserModel> = emptyList(),
//    val isLoading: Boolean = false,
//    val isDeleting: Boolean = false,
//    val error: String? = null,
//    val workerToDelete: UserModel? = null,
//    val showDeleteDialog: Boolean = false,
//    val selectedWorker: UserModel? = null,
//    val showAssignZonesDialog: Boolean = false
//)