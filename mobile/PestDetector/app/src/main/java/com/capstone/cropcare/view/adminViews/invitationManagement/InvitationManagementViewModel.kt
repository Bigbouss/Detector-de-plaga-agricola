package com.capstone.cropcare.view.adminViews.invitationManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.InvitationModel
import com.capstone.cropcare.domain.usecase.invitationUseCase.GenerateInvitationUseCase
import com.capstone.cropcare.domain.usecase.invitationUseCase.GetInvitationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val generateInvitationUseCase: GenerateInvitationUseCase,
    private val getInvitationsUseCase: GetInvitationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvitationState())
    val uiState: StateFlow<InvitationState> = _uiState.asStateFlow()

    init {
        loadInvitations()
    }

    fun loadInvitations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getInvitationsUseCase().collect { invitations ->
                    _uiState.update {
                        it.copy(
                            invitations = invitations,
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

    fun setExpirationDays(days: Int) {
        _uiState.update { it.copy(selectedExpirationDays = days) }
    }

    fun generateInvitation() {
        val expiresInDays = _uiState.value.selectedExpirationDays

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }

            val result = generateInvitationUseCase(expiresInDays)

            result.fold(
                onSuccess = { invitation ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generatedInvitation = invitation,
                            showGeneratedDialog = true
                        )
                    }
                    loadInvitations()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            error = exception.message ?: "Error al generar código"
                        )
                    }
                }
            )
        }
    }

    fun dismissGeneratedDialog() {
        _uiState.update { it.copy(showGeneratedDialog = false, generatedInvitation = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class InvitationState(
    val invitations: List<InvitationModel> = emptyList(),
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val selectedExpirationDays: Int = 7,
    val generatedInvitation: InvitationModel? = null,
    val showGeneratedDialog: Boolean = false,
    val error: String? = null
)