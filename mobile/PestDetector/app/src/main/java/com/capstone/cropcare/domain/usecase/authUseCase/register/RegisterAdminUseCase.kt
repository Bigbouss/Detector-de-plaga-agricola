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
        firstName: String,
        lastName: String,
        companyName: String,
        phone: String? = null,
        taxId: String
    ): Result<UserModel> {
        // Validaciones
        if (email.isBlank()) {
            return Result.failure(Exception("El email no puede estar vacío"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Email inválido"))
        }

        if (password.length < 8) {
            return Result.failure(Exception("La contraseña debe tener al menos 8 caracteres"))
        }

        if (firstName.isBlank()) {
            return Result.failure(Exception("El nombre no puede estar vacío"))
        }

        if (lastName.isBlank()) {
            return Result.failure(Exception("El apellido no puede estar vacío"))
        }

        if (companyName.isBlank()) {
            return Result.failure(Exception("El nombre de la empresa no puede estar vacío"))
        }

        if (taxId.isBlank()) {
            return Result.failure(Exception("El RUT no puede estar vacío"))
        }

        // Validación básica de RUT (formato XX.XXX.XXX-X o XXXXXXXX-X)
        val rutPattern = "^[0-9]{7,8}-[0-9kK]$".toRegex()
        val cleanRut = taxId.replace(".", "").replace(" ", "")
        if (!rutPattern.matches(cleanRut)) {
            return Result.failure(Exception("Formato de RUT inválido. Usa: 12345678-9"))
        }

        // Llamar al repository
        return authRepository.registerAdmin(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            companyName = companyName,
            phone = phone,
            taxId = cleanRut  // Enviar RUT limpio
        )
    }
}