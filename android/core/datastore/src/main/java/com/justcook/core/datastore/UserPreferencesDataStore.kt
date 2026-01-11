package com.justcook.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserPreferences(
    val sessionToken: String? = null,
    val userId: String? = null,
    val userEmail: String? = null,
    val userName: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val preferredLanguage: String = "en"
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val SESSION_TOKEN = stringPreferencesKey("session_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            sessionToken = preferences[PreferencesKeys.SESSION_TOKEN],
            userId = preferences[PreferencesKeys.USER_ID],
            userEmail = preferences[PreferencesKeys.USER_EMAIL],
            userName = preferences[PreferencesKeys.USER_NAME],
            themeMode = preferences[PreferencesKeys.THEME_MODE]?.let {
                ThemeMode.valueOf(it)
            } ?: ThemeMode.SYSTEM,
            preferredLanguage = preferences[PreferencesKeys.PREFERRED_LANGUAGE] ?: "en"
        )
    }

    suspend fun updateSession(token: String, userId: String, email: String, name: String?) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SESSION_TOKEN] = token
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.USER_EMAIL] = email
            name?.let { preferences[PreferencesKeys.USER_NAME] = it }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.SESSION_TOKEN)
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.USER_EMAIL)
            preferences.remove(PreferencesKeys.USER_NAME)
        }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun updatePreferredLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_LANGUAGE] = language
        }
    }
}
