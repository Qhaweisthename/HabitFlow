package com.example.habitflow.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /** Save logged-in user email */
    fun saveUserSession(email: String) {
        prefs.edit()
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    /** Retrieve active user session */
    fun getUserSession(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    /** Clear everything (used for logout) */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    /** Enable / Disable biometric login option */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()
    }

    /** Check if biometrics are allowed for this user */
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    /** Enable / Disable notifications */
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            .apply()
    }

    /** Check if notifications are enabled (default = true) */
    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    /** Check if user has already made a choice */
    fun hasChosenNotificationPref(): Boolean {
        return prefs.contains(KEY_NOTIFICATIONS_ENABLED)
    }


    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

        private const val PREF_NAME = "habitflow_session"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
}
