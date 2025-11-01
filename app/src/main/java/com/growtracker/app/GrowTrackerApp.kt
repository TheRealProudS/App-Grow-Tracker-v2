package com.growtracker.app

import android.app.Application
import com.growtracker.app.util.CrashLogger

class GrowTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Install a global crash handler to capture any early-startup crashes to a file.
        runCatching { CrashLogger.install(this) }
        instance = this
    }

    companion object {
        @Volatile
        private var instance: GrowTrackerApp? = null
        val context get() = checkNotNull(instance) { "GrowTrackerApp not initialized" }
    }
}
