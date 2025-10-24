package com.growtracker.app.ui.ai

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.leafSenseDataStore by preferencesDataStore(name = "leafsense_prefs")

object LeafSensePreferences {
    private val KEY_SHOW_FPS = booleanPreferencesKey("show_fps")

    fun showFpsFlow(context: Context): Flow<Boolean> =
        context.applicationContext.leafSenseDataStore.data.map { prefs ->
            prefs[KEY_SHOW_FPS] ?: true
        }

    suspend fun setShowFps(context: Context, value: Boolean) {
        context.applicationContext.leafSenseDataStore.edit { prefs ->
            prefs[KEY_SHOW_FPS] = value
        }
    }
}