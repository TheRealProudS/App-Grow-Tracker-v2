package com.growtracker.app

import android.os.Bundle
import android.os.StrictMode
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
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.FirebaseOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Modern Splash Screen API
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        // Initialize Firebase if available (no-op if google-services.json is missing).
        // If not configured by google-services.json, try programmatic initialization using BuildConfig values.
        try {
            val existing = FirebaseApp.getApps(this)
            if (existing.isEmpty()) {
                // Attempt default init (works when google-services.json is packaged)
                val defaultApp = FirebaseApp.initializeApp(this)
                if (defaultApp == null) {
                    // Fallback to programmatic init if BuildConfig provides values
                    if (BuildConfig.FIREBASE_APP_ID.isNotBlank() && BuildConfig.FIREBASE_API_KEY.isNotBlank() && BuildConfig.FIREBASE_PROJECT_ID.isNotBlank()) {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                            .setApiKey(BuildConfig.FIREBASE_API_KEY)
                            .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                            .apply {
                                if (BuildConfig.FIREBASE_SENDER_ID.isNotBlank()) setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID)
                                if (BuildConfig.FIREBASE_STORAGE_BUCKET.isNotBlank()) setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET)
                            }
                            .build()
                        FirebaseApp.initializeApp(this, options)
                    }
                }
            }
            // Touch analytics to ensure lazy init in cold start (safe if Analytics is present)
            try { Firebase.analytics.appInstanceId } catch (_: Throwable) {}
        } catch (_: Throwable) {
            // Safe to ignore if Firebase isn't configured locally
        }
        // Enable StrictMode in debug builds to catch bad patterns early
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectLeakedClosableObjects()
                    .detectLeakedSqlLiteObjects()
                    .penaltyLog()
                    .build()
            )
        }
        
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
