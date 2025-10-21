//package com.capstone.cropcare.view.auth.register
//
//import android.util.Patterns
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.capstone.cropcare.domain.model.UserModel
//import com.capstone.cropcare.domain.usecase.authUseCase.register.RegisterAdminUseCase
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class RegisterViewModel @Inject constructor(
//    private val registerAdminUseCase: RegisterAdminUseCase
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(RegisterState())
//    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()
//
//    fun onEmailChanged(email: String) {
//        _uiState.update { it.copy(email = email) }
//        validateForm()
//    }
//
//    fun onPasswordChanged(password: String) {
//        _uiState.update { it.copy(password = password) }
//        validateForm()
//    }
//
//    fun onNameChanged(name: String) {
//        _uiState.update { it.copy(name = name) }
//        validateForm()
//    }
//
//    fun onOrganizationNameChanged(organizationName: String) {
//        _uiState.update { it.copy(organizationName = organizationName) }
//        validateForm()
//    }
//
//    private fun validateForm() {
//        val state = _uiState.value
//
//        val isValidEmail = state.email.isNotBlank() &&
//                Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
//        val isValidPassword = state.password.length >= 8
//        val isValidName = state.name.isNotBlank()
//        val isValidOrgName = state.organizationName.isNotBlank()
//
//        _uiState.update {
//            it.copy(
//                isRegisterEnabled = isValidEmail && isValidPassword &&
//                        isValidName && isValidOrgName
//            )
//        }
//    }
//
//    fun registerAdmin() {
//        if (!_uiState.value.isRegisterEnabled) return
//
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true, error = null) }
//
//            val result = registerAdminUseCase(
//                email = _uiState.value.email.trim(),
//                password = _uiState.value.password,
//                name = _uiState.value.name.trim(),
//                organizationName = _uiState.value.organizationName.trim()
//            )
//
//            result.fold(
//                onSuccess = { user ->
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            registerSuccess = true,
//                            user = user
//                        )
//                    }
//                },
//                onFailure = { exception ->
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            error = exception.message ?: "Error al registrarse"
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
//}
//
//data class RegisterState(
//    val email: String = "",
//    val password: String = "",
//    val name: String = "",
//    val organizationName: String = "",
//    val isRegisterEnabled: Boolean = false,
//    val isLoading: Boolean = false,
//    val registerSuccess: Boolean = false,
//    val user: UserModel? = null,
//    val error: String? = null
//)