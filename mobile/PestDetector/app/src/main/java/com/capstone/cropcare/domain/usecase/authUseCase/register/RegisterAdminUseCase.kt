package com.capstone.cropcare.domain.usecase.authUseCase.register

import android.util.Patterns
import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterAdminUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
        organizationName: String
    ): Result<UserModel> {
        // Validaciones
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Email inválido"))
        }
        if (password.length < 8) {
            return Result.failure(Exception("La contraseña debe tener al menos 8 caracteres"))
        }
        if (name.isBlank()) {
            return Result.failure(Exception("El nombre es requerido"))
        }
        if (organizationName.isBlank()) {
            return Result.failure(Exception("El nombre de la organización es requerido"))
        }

        return authRepository.registerAdmin(email, password, name, organizationName)
    }
}