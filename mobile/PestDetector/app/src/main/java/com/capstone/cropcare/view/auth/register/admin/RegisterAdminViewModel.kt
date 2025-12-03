package com.capstone.cropcare.view.auth.register.admin

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.usecase.authUseCase.register.RegisterAdminUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerAdminUseCase: RegisterAdminUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
        validateForm()
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
        validateForm()
    }

    fun onFirstNameChanged(firstName: String) {
        _uiState.update { it.copy(firstName = firstName) }
        validateForm()
    }

    fun onLastNameChanged(lastName: String) {
        _uiState.update { it.copy(lastName = lastName) }
        validateForm()
    }

    fun onOrganizationNameChanged(organizationName: String) {
        _uiState.update { it.copy(organizationName = organizationName) }
        validateForm()
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone) }
        validateForm()
    }

    fun onTaxIdChanged(taxId: String) {
        _uiState.update { it.copy(taxId = taxId) }
        validateForm()
    }

    private fun validateForm() {
        val state = _uiState.value

        val isValidEmail = state.email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
        val isValidPassword = state.password.length >= 8
        val isValidFirstName = state.firstName.isNotBlank()
        val isValidLastName = state.lastName.isNotBlank()
        val isValidOrgName = state.organizationName.isNotBlank()
        val isValidTaxId = state.taxId.isNotBlank() && validateRut(state.taxId)

        _uiState.update {
            it.copy(
                isRegisterEnabled = isValidEmail && isValidPassword &&
                        isValidFirstName && isValidLastName &&
                        isValidOrgName && isValidTaxId
            )
        }
    }

    private fun validateRut(rut: String): Boolean {
        if (rut.isBlank()) return false

        val rutPattern = """^\d{1,2}\.?\d{3}\.?\d{3}-[\dkK]$""".toRegex()
        return rutPattern.matches(rut)
    }

    private fun cleanRut(rut: String): String {
        return rut.replace(".", "").trim()
    }

    fun registerAdmin() {
        if (!_uiState.value.isRegisterEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = registerAdminUseCase(
                email = _uiState.value.email.trim(),
                password = _uiState.value.password,
                firstName = _uiState.value.firstName.trim(),
                lastName = _uiState.value.lastName.trim(),
                companyName = _uiState.value.organizationName.trim(),
                phone = _uiState.value.phone.trim().takeIf { it.isNotBlank() },
                taxId = cleanRut(_uiState.value.taxId)
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

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val organizationName: String = "",
    val phone: String = "",
    val taxId: String = "",
    val isRegisterEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val registerSuccess: Boolean = false,
    val user: UserModel? = null,
    val error: String? = null
)