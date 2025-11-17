package com.example.habitflow

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.util.Locale

object LocaleManager {

    private const val PREFS_NAME = "habitflow_prefs"
    private const val KEY_LANGUAGE = "app_language"

    const val LANG_ENGLISH = "en"
    const val LANG_AFRIKAANS = "af"
    const val LANG_ZULU = "zu"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSavedLanguage(context: Context): String {
        return prefs(context).getString(KEY_LANGUAGE, LANG_ENGLISH) ?: LANG_ENGLISH
    }

    fun saveLanguage(context: Context, code: String) {
        prefs(context).edit().putString(KEY_LANGUAGE, code).apply()
    }

    /** Wrap context with the currently selected locale */
    fun wrapContext(base: Context): Context {
        val langCode = getSavedLanguage(base)
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val resources = base.resources
        val config = resources.configuration

        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            base.createConfigurationContext(config)
        } else {
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics)
            base
        }
    }
}