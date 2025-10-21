package com.capstone.cropcare.data.repository

import android.util.Log
import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.model.UserRole
import com.capstone.cropcare.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * FAKE REPOSITORY PARA TESTING SIN BACKEND
 */
@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {

    companion object {
        private var sessionUser: UserModel? = null
    }

    private val fakeUsers = mutableMapOf(
        "admin@test.com" to FakeUser(
            email = "admin@test.com",
            password = "admin123",
            user = UserModel(
                uid = "admin_001",
                email = "admin@test.com",
                name = "Admin Test",
                role = UserRole.ADMIN,
                organizationId = "org_001",
                organizationName = "Empresa Test"
            )
        ),
        "worker@test.com" to FakeUser(
            email = "worker@test.com",
            password = "worker123",
            user = UserModel(
                uid = "worker_001",
                email = "worker@test.com",
                name = "Worker Test",
                role = UserRole.WORKER,
                organizationId = "org_001",
                organizationName = "Empresa Test"
            )
        )
    )

    // 👇 CAMBIO: Ahora InvitationData tiene createdAt y expiresAt individuales
    private val validInvitationCodes = mutableMapOf(
        "TEST123" to InvitationData(
            organizationId = "org_001",
            organizationName = "Empresa Test",
            createdBy = "admin_001",
            isUsed = false,
            createdAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5), // 5 días atrás
            expiresAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2) // Expira en 2 días
        ),
        "DEMO456" to InvitationData(
            organizationId = "org_002",
            organizationName = "Agrícola Demo",
            createdBy = "admin_002",
            isUsed = false,
            createdAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10),
            expiresAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(20)
        )
    )

    override suspend fun login(email: String, password: String): Result<UserModel> {
        delay(1000)
        val fakeUser = fakeUsers[email]

        return if (fakeUser != null && fakeUser.password == password) {
            sessionUser = fakeUser.user
            Log.d("FakeAuth", "✅ Login exitoso: ${fakeUser.user.name} (${fakeUser.user.role})")
            Result.success(fakeUser.user)
        } else {
            Log.e("FakeAuth", "❌ Login fallido: credenciales inválidas")
            Result.failure(Exception("Credenciales inválidas"))
        }
    }

    override suspend fun registerAdmin(
        email: String,
        password: String,
        name: String,
        organizationName: String
    ): Result<UserModel> {
        delay(1500)

        if (fakeUsers.containsKey(email)) {
            return Result.failure(Exception("El email ya está registrado"))
        }

        val orgId = "org_${System.currentTimeMillis()}"
        val newUser = UserModel(
            uid = "admin_${System.currentTimeMillis()}",
            email = email,
            name = name,
            role = UserRole.ADMIN,
            organizationId = orgId,
            organizationName = organizationName
        )

        fakeUsers[email] = FakeUser(email, password, newUser)
        sessionUser = newUser

        Log.d("FakeAuth", "✅ Admin registrado: $name - $organizationName")
        return Result.success(newUser)
    }

    override suspend fun registerWorker(
        email: String,
        password: String,
        name: String,
        invitationCode: String,
        phoneNumber: String // 👈 agrégalo aquí
    ): Result<UserModel> {
        delay(1500)

        if (fakeUsers.containsKey(email)) {
            return Result.failure(Exception("El email ya está registrado"))
        }

        val invitationData = validInvitationCodes[invitationCode]
            ?: return Result.failure(Exception("Código de invitación inválido"))

        if (invitationData.isUsed) {
            return Result.failure(Exception("Este código ya fue utilizado"))
        }

        if (System.currentTimeMillis() > invitationData.expiresAt) {
            return Result.failure(Exception("Este código ya expiró"))
        }

        val newUser = UserModel(
            uid = "worker_${System.currentTimeMillis()}",
            email = email,
            name = name,
            role = UserRole.WORKER,
            organizationId = invitationData.organizationId,
            organizationName = invitationData.organizationName
        )

        fakeUsers[email] = FakeUser(email, password, newUser)
        sessionUser = newUser

        validInvitationCodes[invitationCode] = invitationData.copy(isUsed = true)

        Log.d("FakeAuth", "✅ Worker registrado: $name en ${invitationData.organizationName}")
        return Result.success(newUser)
    }


    override suspend fun validateInvitationCode(code: String): Result<String> {
        delay(500)

        val invitationData = validInvitationCodes[code]

        return when {
            invitationData == null -> {
                Log.e("FakeAuth", "❌ Código inválido: $code")
                Result.failure(Exception("Código de invitación no válido"))
            }
            invitationData.isUsed -> {
                Log.e("FakeAuth", "❌ Código ya usado: $code")
                Result.failure(Exception("Este código ya fue utilizado"))
            }
            System.currentTimeMillis() > invitationData.expiresAt -> {
                Log.e("FakeAuth", "❌ Código expirado: $code")
                Result.failure(Exception("Este código ya expiró"))
            }
            else -> {
                Log.d("FakeAuth", "✅ Código válido: $code -> ${invitationData.organizationName}")
                Result.success(invitationData.organizationName)
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        delay(300)
        sessionUser = null
        Log.d("FakeAuth", "✅ Logout exitoso")
        return Result.success(Unit)
    }

    override suspend fun refreshAccessToken(): Result<String> {
        delay(500)
        return Result.success("fake_access_token")
    }

    override suspend fun getCurrentUser(): UserModel? {
        Log.d("FakeAuth", "🔍 getCurrentUser: ${sessionUser?.name ?: "null"}")
        return sessionUser
    }

    override fun isUserLoggedIn(): Boolean {
        val isLoggedIn = sessionUser != null
        Log.d("FakeAuth", "🔍 isUserLoggedIn: $isLoggedIn")
        return isLoggedIn
    }

    // ========== FUNCIONES DE INVITACIONES ==========
    fun generateInvitationCode(
        adminId: String,
        organizationId: String,
        organizationName: String,
        expiresInDays: Int = 7 // 👈 Parámetro para controlar expiración
    ): InvitationInfo {
        val code = generateRandomCode(6)
        val now = System.currentTimeMillis()
        val expiresAt = now + TimeUnit.DAYS.toMillis(expiresInDays.toLong())

        validInvitationCodes[code] = InvitationData(
            organizationId = organizationId,
            organizationName = organizationName,
            createdBy = adminId,
            isUsed = false,
            createdAt = now, // 👈 Cada código tiene su propia fecha
            expiresAt = expiresAt
        )

        Log.d("FakeAuth", "✅ Código generado: $code para $organizationName (expira en $expiresInDays días)")

        return InvitationInfo(
            code = code,
            organizationName = organizationName,
            isUsed = false,
            createdAt = now,
            expiresAt = expiresAt
        )
    }

    fun getAllInvitationsForOrganization(organizationId: String): List<InvitationInfo> {
        return validInvitationCodes
            .filter { it.value.organizationId == organizationId }
            .map { (code, data) ->
                InvitationInfo(
                    code = code,
                    organizationName = data.organizationName,
                    isUsed = data.isUsed,
                    createdAt = data.createdAt, // 👈 Fecha individual
                    expiresAt = data.expiresAt
                )
            }
            .sortedByDescending { it.createdAt } // Más recientes primero
    }

    // 👇 NUEVA: Función para contar códigos activos
    fun getActiveInvitationsCount(organizationId: String): Int {
        val now = System.currentTimeMillis()
        return validInvitationCodes.count { (_, data) ->
            data.organizationId == organizationId &&
                    !data.isUsed &&
                    data.expiresAt > now
        }
    }

    private fun generateRandomCode(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var code: String

        // Asegurar que el código sea único
        do {
            code = (1..length)
                .map { chars[Random.nextInt(chars.length)] }
                .joinToString("")
        } while (validInvitationCodes.containsKey(code))

        return code
    }

    private data class FakeUser(
        val email: String,
        val password: String,
        val user: UserModel
    )

    private data class InvitationData(
        val organizationId: String,
        val organizationName: String,
        val createdBy: String,
        val isUsed: Boolean,
        val createdAt: Long = System.currentTimeMillis(), // 👈 Individual
        val expiresAt: Long
    )

    data class InvitationInfo(
        val code: String,
        val organizationName: String,
        val isUsed: Boolean,
        val createdAt: Long = System.currentTimeMillis(), // 👈 Individual
        val expiresAt: Long
    )
}