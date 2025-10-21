package com.capstone.cropcare.domain.usecase.authUseCase.register
import android.util.Patterns
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
        phoneNumber: String,       // ← nuevo parámetro
        invitationCode: String
    ): Result<UserModel> {
        // === Validaciones ===
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Email inválido"))
        }

        if (password.length < 8) {
            return Result.failure(Exception("La contraseña debe tener al menos 8 caracteres"))
        }

        if (!password.any { it.isDigit() }) {
            return Result.failure(Exception("La contraseña debe contener al menos un número"))
        }

        if (!password.any { it.isUpperCase() }) {
            return Result.failure(Exception("La contraseña debe contener al menos una mayúscula"))
        }

        if (name.isBlank() || name.length < 3) {
            return Result.failure(Exception("El nombre debe tener al menos 3 caracteres"))
        }

        if (phoneNumber.isBlank() || phoneNumber.length < 8) {
            return Result.failure(Exception("El número de teléfono no es válido"))
        }

        if (invitationCode.isBlank() || invitationCode.length < 6) {
            return Result.failure(Exception("Código de invitación inválido"))
        }

        // === Llamada al repositorio ===
        return authRepository.registerWorker(
            email = email,
            password = password,
            name = name,
            phoneNumber = phoneNumber,     // ← agregado aquí
            invitationCode = invitationCode
        )
    }
}
