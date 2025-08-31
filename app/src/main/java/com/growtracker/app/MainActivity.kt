package com.growtracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.growtracker.app.ui.GrowTrackerApp
import com.growtracker.app.ui.theme.GrowTrackerTheme
import com.growtracker.app.ui.theme.ThemeManager
import com.growtracker.app.ui.language.LanguageManager
import com.growtracker.app.ui.language.LocalLanguageManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Modern Splash Screen API
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Modern edge-to-edge design
        enableEdgeToEdge()
        
        // Splash screen delay with modern API
        splashScreen.setKeepOnScreenCondition { 
            false // Can be controlled by app state
        }
        
        initializeApp()
    }
    
    private fun initializeApp() {
        setContent {
            val themeManager = remember { ThemeManager() }
            val languageManager = remember { LanguageManager() }
            
            CompositionLocalProvider(LocalLanguageManager provides languageManager) {
                GrowTrackerTheme(themeManager = themeManager) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        GrowTrackerApp(
                            modifier = Modifier.padding(innerPadding),
                            themeManager = themeManager,
                            languageManager = languageManager
                        )
                    }
                }
            }
        }
    }
}
