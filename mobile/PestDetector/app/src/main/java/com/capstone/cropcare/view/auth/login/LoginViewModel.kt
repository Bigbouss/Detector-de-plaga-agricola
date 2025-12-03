package com.capstone.cropcare.view.auth.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.usecase.authUseCase.login.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
        validateForm()
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
        validateForm()
    }

    private fun validateForm() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        val isValidEmail = email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isValidPassword = password.length >= 6

        _uiState.update {
            it.copy(isLoginEnabled = isValidEmail && isValidPassword)
        }
    }

    fun login() {
        if (!_uiState.value.isLoginEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = loginUseCase(
                email = _uiState.value.email.trim(),
                password = _uiState.value.password
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginSuccess = true,
                            user = user
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al iniciar sesión"
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

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoginEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val user: UserModel? = null,
    val error: String? = null
)