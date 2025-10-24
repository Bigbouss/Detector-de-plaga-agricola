package com.capstone.cropcare.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
        private val KEY_UID = stringPreferencesKey("user_uid")
        private val KEY_EMAIL = stringPreferencesKey("user_email")
        private val KEY_NAME = stringPreferencesKey("user_name")
        private val KEY_ROLE = stringPreferencesKey("user_role")
        private val KEY_ORG_ID = stringPreferencesKey("org_id")
        private val KEY_ORG_NAME = stringPreferencesKey("org_name")
        private val KEY_MUST_CHANGE_PWD = booleanPreferencesKey("must_change_password")
    }

    val userFlow: Flow<UserModel?> = dataStore.data.map { prefs ->
        val uid = prefs[KEY_UID]
        if (uid.isNullOrEmpty()) {
            null
        } else {
            UserModel(
                uid = uid,
                email = prefs[KEY_EMAIL] ?: "",
                name = prefs[KEY_NAME] ?: "",
                role = when (prefs[KEY_ROLE]) {
                    "ADMIN" -> UserRole.ADMIN
                    "WORKER" -> UserRole.WORKER
                    else -> UserRole.WORKER
                },
                organizationId = prefs[KEY_ORG_ID] ?: "",
                organizationName = prefs[KEY_ORG_NAME] ?: "",
                mustChangePassword = prefs[KEY_MUST_CHANGE_PWD] ?: false
            )
        }
    }

    suspend fun saveUser(user: UserModel) {
        dataStore.edit { prefs ->
            prefs[KEY_UID] = user.uid
            prefs[KEY_EMAIL] = user.email
            prefs[KEY_NAME] = user.name
            prefs[KEY_ROLE] = user.role.name
            prefs[KEY_ORG_ID] = user.organizationId
            prefs[KEY_ORG_NAME] = user.organizationName
            prefs[KEY_MUST_CHANGE_PWD] = user.mustChangePassword
        }
    }

    suspend fun clearUser() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}