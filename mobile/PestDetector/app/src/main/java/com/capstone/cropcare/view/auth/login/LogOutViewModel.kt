package com.capstone.cropcare.view.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.usecase.authUseCase.login.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.Loading

            logoutUseCase().fold(
                onSuccess = {
                    _logoutState.value = LogoutState.Success
                },
                onFailure = { error ->
                    // Incluso si falla, el repositorio ya limpió los datos locales
                    _logoutState.value = LogoutState.Success
                }
            )
        }
    }

    fun resetLogoutState() {
        _logoutState.value = LogoutState.Idle
    }
}

sealed class LogoutState {
    data object Idle : LogoutState()
    data object Loading : LogoutState()
    data object Success : LogoutState()
    data object Error : LogoutState()
}