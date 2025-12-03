package com.capstone.cropcare.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.capstone.cropcare.domain.model.UserModel
import com.capstone.cropcare.domain.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val USER_ID = intPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val EMPRESA_ID = intPreferencesKey("empresa_id")
    }

    val userFlow: Flow<UserModel?> = dataStore.data.map { preferences ->
        val userId = preferences[USER_ID]
        val email = preferences[USER_EMAIL]
        val roleStr = preferences[USER_ROLE]
        val empresaId = preferences[EMPRESA_ID]

        if (userId != null && email != null && roleStr != null && empresaId != null) {
            UserModel(
                id = userId,
                email = email,
                role = when (roleStr) {
                    "ADMIN" -> UserRole.ADMIN
                    "WORKER" -> UserRole.WORKER
                    else -> UserRole.WORKER
                },
                empresaId = empresaId
            )
        } else {
            null
        }
    }

    suspend fun saveUser(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = user.id
            preferences[USER_EMAIL] = user.email
            preferences[USER_ROLE] = user.role.name
            preferences[EMPRESA_ID] = user.empresaId
        }
    }

    suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID)
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_ROLE)
            preferences.remove(EMPRESA_ID)
        }
    }
}