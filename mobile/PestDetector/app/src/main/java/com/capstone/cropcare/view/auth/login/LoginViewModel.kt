package com.capstone.cropcare.view.auth.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.capstone.cropcare.domain.usecase.LoginUserCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/*******************************************************************************************
 * _uiState es privado y mutable: solo el ViewModel puede modificarlo.                     *
 * uiState es público y de solo lectura: las vistas/composables lo observan.               *
 * esto hace qye no pueden cambiar su valor directamente (uiState).                        *
 * De esta forma, la lógica de actualización de estado queda centralizada en el ViewModel  *
 *******************************************************************************************/

@HiltViewModel
class LoginViewModel @Inject constructor (val login: LoginUserCase): ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChanged(email: String){
        _uiState.update { state ->
            state.copy(email = email)
        }
        LoginValidation()
    }

    fun onPasswordChanged(password: String){
        _uiState.update { state ->
            state.copy(password = password)
        }
        LoginValidation()
    }

    private fun LoginValidation(){
        val isEnabledLogin: Boolean = isEmailValid(_uiState.value.email) && isPasswordValid(_uiState.value.password)

        _uiState.update {
            it.copy(isLoginEnable = isEnabledLogin)
        }
    }

    //VALIDATION FUNCTION
    fun isEmailValid(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun isPasswordValid(password: String): Boolean = password.length>= 6
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginEnable: Boolean = false,
)