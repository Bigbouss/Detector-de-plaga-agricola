package com.capstone.cropcare.view.auth.register.worker

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.usecase.authUseCase.register.RegisterWorkerUseCase
import com.capstone.cropcare.domain.usecase.invitationUseCase.ValidateInvitationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterWorkerViewModel @Inject constructor(
    private val registerWorkerUseCase: RegisterWorkerUseCase,
    private val validateInvitationUseCase: ValidateInvitationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterWorkerState())
    val uiState: StateFlow<RegisterWorkerState> = _uiState.asStateFlow()

    fun onInvitationCodeChanged(code: String) {
        _uiState.update { it.copy(invitationCode = code.uppercase()) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
        validateForm()
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
        validateForm()
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
        validateForm()
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
        validateForm()
    }


    fun validateInvitationCode() {
        val code = _uiState.value.invitationCode
        if (code.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isValidatingCode = true, codeError = null) }

            val result = validateInvitationUseCase(code)

            result.fold(
                onSuccess = { organizationName ->
                    _uiState.update {
                        it.copy(
                            isValidatingCode = false,
                            isCodeValid = true,
                            organizationName = organizationName
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isValidatingCode = false,
                            isCodeValid = false,
                            codeError = exception.message ?: "Código inválido"
                        )
                    }
                }
            )
        }
    }

    private fun validateForm() {
        val state = _uiState.value

        val isValidEmail = state.email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
        val isValidPassword = state.password.length >= 8
        val isValidName = state.name.isNotBlank()
        val isValidPhone = state.phoneNumber.length >= 8
        val hasValidCode = state.isCodeValid

        _uiState.update {
            it.copy(
                isRegisterEnabled = isValidEmail && isValidPassword &&
                        isValidName && isValidPhone && hasValidCode
            )
        }
    }


    fun registerWorker() {
        if (!_uiState.value.isRegisterEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = registerWorkerUseCase(
                email = _uiState.value.email.trim(),
                password = _uiState.value.password,
                name = _uiState.value.name.trim(),
                phoneNumber = _uiState.value.phoneNumber.trim(),
                invitationCode = _uiState.value.invitationCode.trim()
            )


            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            registerSuccess = true,
                            user = user
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al registrarse"
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

data class RegisterWorkerState(
    val invitationCode: String = "",
    val isCodeValid: Boolean = false,
    val isValidatingCode: Boolean = false,
    val codeError: String? = null,
    val organizationName: String? = null,

    val email: String = "",
    val password: String = "",
    val name: String = "",
    val phoneNumber: String = "",

    val isRegisterEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val registerSuccess: Boolean = false,
    val user: UserModel? = null,
    val error: String? = null
)
