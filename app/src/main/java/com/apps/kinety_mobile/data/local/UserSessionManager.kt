package com.apps.kinety_mobile.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.apps.kinety_mobile.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kinety_session")

class UserSessionManager(private val context: Context) {

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_LAST_PROJECT_ID = stringPreferencesKey("last_project_id")
        private val KEY_BACKEND_URL = stringPreferencesKey("backend_url")
        private val KEY_API_TOKEN = stringPreferencesKey("api_token")
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val rememberMeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_REMEMBER_ME] ?: true
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: "Usuario Kinety"
    }

    val userEmailFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL] ?: ""
    }

    val userIdFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID] ?: ""
    }

    val themeModeFlow: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_THEME_MODE]) {
            AppThemeMode.PEARL_SILVER.name -> AppThemeMode.PEARL_SILVER
            AppThemeMode.SYSTEM.name -> AppThemeMode.SYSTEM
            else -> AppThemeMode.MERLOT_NOIR
        }
    }

    val lastProjectIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_PROJECT_ID]
    }

    val backendUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_BACKEND_URL] ?: "http://10.0.2.2:8080"
    }

    val apiTokenFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_API_TOKEN] ?: ""
    }

    suspend fun saveSession(
        userId: String,
        name: String,
        email: String,
        rememberMe: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_REMEMBER_ME] = rememberMe
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setLastProjectId(projectId: String?) {
        context.dataStore.edit { prefs ->
            if (projectId != null) {
                prefs[KEY_LAST_PROJECT_ID] = projectId
            } else {
                prefs.remove(KEY_LAST_PROJECT_ID)
            }
        }
    }

    suspend fun saveBackendConfig(url: String, token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BACKEND_URL] = url.trim()
            prefs[KEY_API_TOKEN] = token.trim()
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_EMAIL)
        }
    }
}
