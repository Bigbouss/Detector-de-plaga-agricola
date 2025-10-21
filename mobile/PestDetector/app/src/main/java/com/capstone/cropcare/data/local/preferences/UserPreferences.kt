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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_NAME = stringPreferencesKey("name")
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_ORGANIZATION_ID = stringPreferencesKey("organization_id")
        private val KEY_ORGANIZATION_NAME = stringPreferencesKey("organization_name")
    }

    suspend fun saveUser(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = user.uid
            preferences[KEY_EMAIL] = user.email
            preferences[KEY_NAME] = user.name
            preferences[KEY_ROLE] = user.role.name
            preferences[KEY_ORGANIZATION_ID] = user.organizationId
            preferences[KEY_ORGANIZATION_NAME] = user.organizationName
        }
    }

    val userFlow: Flow<UserModel?> = dataStore.data.map { preferences ->
        val userId = preferences[KEY_USER_ID]
        val email = preferences[KEY_EMAIL]
        val name = preferences[KEY_NAME]
        val roleString = preferences[KEY_ROLE]
        val orgId = preferences[KEY_ORGANIZATION_ID]
        val orgName = preferences[KEY_ORGANIZATION_NAME]

        if (userId != null && email != null && name != null && roleString != null && orgId != null && orgName != null) {
            UserModel(
                uid = userId,
                email = email,
                name = name,
                role = UserRole.valueOf(roleString),
                organizationId = orgId,
                organizationName = orgName
            )
        } else {
            null
        }
    }

    suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}