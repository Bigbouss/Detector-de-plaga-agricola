package com.capstone.cropcare.domain.manager

import com.capstone.cropcare.data.local.preferences.UserPreferences
import com.capstone.cropcare.domain.model.UserModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    private val userPreferences: UserPreferences
) {
    suspend fun getCurrentUser(): UserModel? {
        return try {
            userPreferences.userFlow.first()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getEmpresaId(): Int {
        return getCurrentUser()?.empresaId ?: -1
    }

    suspend fun getUserId(): Int {
        return getCurrentUser()?.id ?: -1
    }

    suspend fun getWorkerName(): String {
        return getCurrentUser()?.email?.substringBefore("@") ?: "Worker"
    }
}