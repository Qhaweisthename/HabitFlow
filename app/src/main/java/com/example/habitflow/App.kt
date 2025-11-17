package com.example.habitflow

import android.app.Application
import android.content.Context

class App : Application() {

    override fun attachBaseContext(base: Context) {
        // Wrap the whole app with the saved language
        super.attachBaseContext(LocaleManager.wrapContext(base))
    }

    override fun onCreate() {
        super.onCreate()
        // Ensure locale is applied on app start
        LocaleManager.wrapContext(this)
    }
}