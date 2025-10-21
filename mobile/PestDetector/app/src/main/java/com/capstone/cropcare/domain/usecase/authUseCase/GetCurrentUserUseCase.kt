package com.capstone.cropcare.domain.usecase.authUseCase

import android.util.Log
import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): UserModel? {
        val user = authRepository.getCurrentUser()
        Log.d("GetCurrentUserUseCase", "Usuario actual: ${user?.name} (${user?.role})")
        return user
    }
}