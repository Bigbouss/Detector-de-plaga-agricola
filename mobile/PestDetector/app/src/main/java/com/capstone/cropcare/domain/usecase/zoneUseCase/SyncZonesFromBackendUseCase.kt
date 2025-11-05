package com.capstone.cropcare.domain.usecase.zoneUseCase

import com.capstone.cropcare.domain.model.UserRole
import com.capstone.cropcare.domain.repository.AuthRepository
import com.capstone.cropcare.domain.repository.CropZoneRepository
import javax.inject.Inject

/**
 * Sincroniza zonas desde el backend según el rol del usuario:
 * - ADMIN: Sincroniza todas las zonas de su empresa
 * - WORKER: Sincroniza solo las zonas asignadas a él
 */
class SyncZonesFromBackendUseCase @Inject constructor(
    private val cropZoneRepository: CropZoneRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val currentUser = authRepository.getCurrentUser()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            when (currentUser.role) {
                UserRole.ADMIN -> {
                    // Admin: sincronizar todas las zonas de la empresa
                    cropZoneRepository.syncAllZonesFromBackend()
                }
                UserRole.WORKER -> {
                    // Worker: sincronizar solo zonas asignadas
                    cropZoneRepository.syncAssignedZonesFromBackend()
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}