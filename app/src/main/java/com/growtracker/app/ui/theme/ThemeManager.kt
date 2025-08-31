package com.growtracker.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ThemeManager {
    private var _isDarkMode by mutableStateOf(true) // Immer Dark Mode aktiviert
    
    val isDarkMode: Boolean
        get() = _isDarkMode
    
    fun toggleDarkMode() {
        _isDarkMode = !_isDarkMode
    }
    
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode = enabled
    }
}

val LocalThemeManager = staticCompositionLocalOf { ThemeManager() }
