package com.example.habitflow.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveUserSession(email: String) {
        prefs.edit()
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getUserSession(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    // ✅ Biometric preference
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    companion object {
        private const val PREF_NAME = "habitflow_session"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
}
