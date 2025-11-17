package com.example.habitflow

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.habitflow.util.AppUsageTracker

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Track when app moves foreground ↔ background
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> AppUsageTracker.onResume(this) // app to foreground
                    Lifecycle.Event.ON_STOP  -> AppUsageTracker.onPause(this)  // app to background
                    else -> Unit
                }
            }
        )
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
