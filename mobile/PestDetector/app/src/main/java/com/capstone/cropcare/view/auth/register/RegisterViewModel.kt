package com.capstone.cropcare.view.auth.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel(){
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState


    fun onEmailChanged(email: String){
        _uiState.update { state ->
            state.copy(email = email)
        }
        RegisterValidation()
    }

    fun onPasswordChanged(password: String){
        _uiState.update { state ->
            state.copy(password = password)
        }
        RegisterValidation()
    }
    

    fun RegisterValidation(){
        val isEnabledRegister: Boolean = isEmailValid(_uiState.value.email) && isPasswordValid(_uiState.value.password)

        _uiState.update {
            it.copy(isRegisterEnable = isEnabledRegister)
        }
    }

    //VALIDATION FUNCTION
    fun isEmailValid(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun isPasswordValid(password: String): Boolean = password.length>= 6
}

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isRegisterEnable: Boolean = false,
)