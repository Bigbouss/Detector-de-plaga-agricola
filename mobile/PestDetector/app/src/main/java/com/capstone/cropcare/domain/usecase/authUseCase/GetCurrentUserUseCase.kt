package com.capstone.cropcare.domain.usecase.authUseCase

import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): UserModel? {
        return authRepository.getCurrentUser()
    }
}