package com.capstone.cropcare.domain.usecase.authUseCase.login

import com.capstone.cropcare.domain.repository.AuthRepository
import javax.inject.Inject

class IsUserLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}