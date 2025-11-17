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
        // Wrap context with saved language
        super.attachBaseContext(LocaleManager.wrapContext(base))
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Apply locale at startup
        LocaleManager.wrapContext(this)

        // Track foreground/background app usage
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> AppUsageTracker.onResume(this) // app enters foreground
                    Lifecycle.Event.ON_STOP  -> AppUsageTracker.onPause(this)  // app goes background
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
