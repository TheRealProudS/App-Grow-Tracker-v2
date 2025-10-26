package com.growtracker.app.data.consent

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for storing user consent to upload anonymized plant analysis images.
 * Opt-in only; default = false.
 */
class DataUploadConsentRepository(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "user_prefs")

    private val KEY_CONSENT = booleanPreferencesKey("data_upload_consent")

    val consentFlow: Flow<Boolean> = context.dataStore.data.map { prefs: Preferences ->
        prefs[KEY_CONSENT] ?: false
    }

    suspend fun setConsent(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CONSENT] = enabled
        }
    }
}
