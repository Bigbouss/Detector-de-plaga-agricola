package com.capstone.cropcare.domain.usecase.authUseCase.register

import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterWorkerUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
        phoneNumber: String,
        invitationCode: String
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

        if (phoneNumber.length < 8) {
            return Result.failure(Exception("El número de teléfono debe tener al menos 8 dígitos"))
        }

        if (invitationCode.isBlank()) {
            return Result.failure(Exception("El código de invitación no puede estar vacío"))
        }

        // Llamar al repository
        return authRepository.registerWorker(
            email = email,
            password = password,
            name = name,
            invitationCode = invitationCode,
            phoneNumber = phoneNumber
        )
    }
}