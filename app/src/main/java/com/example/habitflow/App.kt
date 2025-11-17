package com.example.habitflow

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.habitflow.util.AppUsageTracker
import com.example.habitflow.util.LocaleManager

class App : Application() {

    override fun attachBaseContext(base: Context) {
        // Wrap the app with the saved language preference
        super.attachBaseContext(LocaleManager.wrapContext(base))
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Apply locale on app start
        LocaleManager.wrapContext(this)

        // Track app foreground/background usage
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
