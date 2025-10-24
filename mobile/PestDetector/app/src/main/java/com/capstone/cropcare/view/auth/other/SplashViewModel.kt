package com.capstone.cropcare.view.auth.other

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.cropcare.domain.model.UserRole
import com.capstone.cropcare.domain.usecase.authUseCase.GetCurrentUserUseCase
import com.capstone.cropcare.domain.usecase.authUseCase.login.IsUserLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase // 👈 Agregar este UseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashState>(SplashState.Loading)
    val uiState: StateFlow<SplashState> = _uiState.asStateFlow()

    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            // Tiempo mínimo para que la animación se vea completa
            delay(1000)

            // 1️⃣ Verificar si hay tokens válidos
            val hasValidTokens = isUserLoggedInUseCase()

            // 2️⃣ Solo si hay tokens, obtener el usuario
            val currentUser = if (hasValidTokens) {
                getCurrentUserUseCase()
            } else {
                null
            }

            // 3️⃣ Determinar el estado
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