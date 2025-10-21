package com.capstone.cropcare.view.auth.other

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.UserRole
import com.capstone.cropcare.domain.usecase.authUseCase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashState>(SplashState.Loading)
    val uiState: StateFlow<SplashState> = _uiState.asStateFlow()

    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            // Tiempo mínimo para que la animación se vea completa
            // Ajusta este valor según la duración de tu animación Lottie
            delay(1000) // Mínimo 1 segundo para que se vea la animación

            val currentUser = getCurrentUserUseCase()

            _uiState.value = if (currentUser != null) {
                SplashState.Authenticated(currentUser.role)
            } else {
                SplashState.NotAuthenticated
            }
        }
    }
}

sealed class SplashState {
    object Loading : SplashState()
    object NotAuthenticated : SplashState()
    data class Authenticated(val userRole: UserRole) : SplashState()
}