package com.example.habitflow.util

import android.content.Context
import android.os.SystemClock

object AppUsageTracker {
    private const val PREF = "app_usage"
    private const val KEY_TOTAL_MS = "total_ms"
    private const val KEY_SESSION_START = "session_start"
    private const val KEY_IS_RUNNING = "is_running"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun onResume(ctx: Context) {
        val p = prefs(ctx)
        if (!p.getBoolean(KEY_IS_RUNNING, false)) {
            p.edit()
                .putLong(KEY_SESSION_START, SystemClock.elapsedRealtime())
                .putBoolean(KEY_IS_RUNNING, true)
                .apply()
        }
    }

    fun onPause(ctx: Context) {
        val p = prefs(ctx)
        if (p.getBoolean(KEY_IS_RUNNING, false)) {
            val start = p.getLong(KEY_SESSION_START, 0L)
            val delta = (SystemClock.elapsedRealtime() - start).coerceAtLeast(0L)
            val total = p.getLong(KEY_TOTAL_MS, 0L) + delta
            p.edit()
                .putLong(KEY_TOTAL_MS, total)
                .putBoolean(KEY_IS_RUNNING, false)
                .apply()
        }
    }

    fun isRunning(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_IS_RUNNING, false)
    fun getTotalMs(ctx: Context): Long = prefs(ctx).getLong(KEY_TOTAL_MS, 0L)
    fun getSessionStart(ctx: Context): Long = prefs(ctx).getLong(KEY_SESSION_START, 0L)

    /** Total including the ongoing foreground session (if running). */
    fun getCurrentTotalMs(ctx: Context): Long {
        val base = getTotalMs(ctx)
        return if (isRunning(ctx)) {
            val start = getSessionStart(ctx)
            base + (SystemClock.elapsedRealtime() - start).coerceAtLeast(0L)
        } else base
    }

    fun reset(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }
}
