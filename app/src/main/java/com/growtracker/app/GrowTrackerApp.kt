package com.growtracker.app

import android.app.Application

class GrowTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile
        private var instance: GrowTrackerApp? = null
        val context get() = checkNotNull(instance) { "GrowTrackerApp not initialized" }
    }
}
