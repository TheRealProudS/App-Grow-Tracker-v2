package com.growtracker.app.ui.language

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

enum class Language(val code: String, val displayName: String, val flag: String) {
    GERMAN("de", "Deutsch", "🇩🇪"),
    ENGLISH("en", "English", "🇬🇧"),
    SPANISH("es", "Español", "🇪🇸"),
    FRENCH("fr", "Français", "🇫🇷"),
    ITALIAN("it", "Italiano", "🇮🇹")
}

class LanguageManager {
    private var _currentLanguage by mutableStateOf(Language.GERMAN)
    
    val currentLanguage: Language
        get() = _currentLanguage
    
    fun setLanguage(language: Language) {
        _currentLanguage = language
    }
    
    fun getAllLanguages(): List<Language> {
        return Language.values().toList()
    }
}

val LocalLanguageManager = staticCompositionLocalOf { LanguageManager() }
