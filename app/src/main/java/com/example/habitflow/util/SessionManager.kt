package com.example.habitflow.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("habitflow_prefs", Context.MODE_PRIVATE)

    // ✅ Save session (email)
    fun saveUserSession(email: String) {
        prefs.edit().putString("logged_in_user", email).apply()
    }

    // ✅ Retrieve user session (returns email or null)
    fun getUserSession(): String? {
        return prefs.getString("logged_in_user", null)
    }

    // ✅ Clear entire session
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
