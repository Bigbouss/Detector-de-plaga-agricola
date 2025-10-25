package com.capstone.cropcare.domain.usecase.invitationUseCase

import com.capstone.cropcare.domain.repository.AuthRepository
import javax.inject.Inject

class ValidateInvitationUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(code: String): Result<String> {
        if (code.isBlank()) {
            return Result.failure(Exception("El código no puede estar vacío"))
        }

        if (code.length < 6) {
            return Result.failure(Exception("El código debe tener al menos 6 caracteres"))
        }

        return authRepository.validateInvitationCode(code)
    }
}