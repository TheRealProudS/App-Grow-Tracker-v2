package com.growtracker.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

class AuthPreferencesManager(private val context: Context) {
    
    private object PreferencesKeys {
        val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val LAST_LOGIN_TIMESTAMP = longPreferencesKey("last_login_timestamp")
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
        val SESSION_TOKEN = stringPreferencesKey("session_token")
    }
    
    val currentUserId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CURRENT_USER_ID]
    }
    
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
    }
    
    val lastLoginTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_LOGIN_TIMESTAMP] ?: 0L
    }
    
    val rememberMe: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMEMBER_ME] ?: false
    }
    
    val sessionToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SESSION_TOKEN]
    }
    
    suspend fun saveUserSession(
        userId: String,
        rememberMe: Boolean = true,
        sessionToken: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_USER_ID] = userId
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
            preferences[PreferencesKeys.LAST_LOGIN_TIMESTAMP] = System.currentTimeMillis()
            preferences[PreferencesKeys.REMEMBER_ME] = rememberMe
            sessionToken?.let {
                preferences[PreferencesKeys.SESSION_TOKEN] = it
            }
        }
    }
    
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.CURRENT_USER_ID)
            preferences.remove(PreferencesKeys.IS_LOGGED_IN)
            preferences.remove(PreferencesKeys.SESSION_TOKEN)
            preferences[PreferencesKeys.LAST_LOGIN_TIMESTAMP] = System.currentTimeMillis()
        }
    }
    
    suspend fun updateRememberMe(remember: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMEMBER_ME] = remember
        }
    }
}
