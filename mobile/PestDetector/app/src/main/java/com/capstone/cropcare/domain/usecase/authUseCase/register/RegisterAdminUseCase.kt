package com.capstone.cropcare.domain.usecase.authUseCase.register

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
        if (email.isBlank()) {
            return Result.failure(Exception("El email no puede estar vacío"))
        }

        if (password.length < 8) {
            return Result.failure(Exception("La contraseña debe tener al menos 8 caracteres"))
        }

        if (name.isBlank()) {
            return Result.failure(Exception("El nombre no puede estar vacío"))
        }

        if (organizationName.isBlank()) {
            return Result.failure(Exception("El nombre de la organización no puede estar vacío"))
        }

        // Llamar al repository
        return authRepository.registerAdmin(
            email = email,
            password = password,
            name = name,
            organizationName = organizationName
        )
    }
}