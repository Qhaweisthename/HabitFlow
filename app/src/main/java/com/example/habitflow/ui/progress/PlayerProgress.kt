package com.example.habitflow.ui.progress

import android.content.Context
import android.content.SharedPreferences

class PlayerProgress private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var level: Int
        get() = prefs.getInt(KEY_LEVEL, 0)
        private set(value) {
            prefs.edit().putInt(KEY_LEVEL, value).apply()
        }

    var xp: Int
        get() = prefs.getInt(KEY_XP, 0)
        private set(value) {
            prefs.edit().putInt(KEY_XP, value).apply()
        }

    fun addExperience(amount: Int) {
        if (amount <= 0) return
        var remaining = amount
        var curLevel = level
        var curXp = xp

        while (remaining > 0 && curLevel < MAX_LEVEL) {
            val space = XP_PER_LEVEL - curXp
            if (remaining >= space) {
                // level up
                remaining -= space
                curLevel += 1
                curXp = 0
            } else {
                curXp += remaining
                remaining = 0
            }
        }
        // If at max level, cap xp to full bar
        if (curLevel >= MAX_LEVEL) {
            curLevel = MAX_LEVEL
            curXp = XP_PER_LEVEL
        }
        level = curLevel
        xp = curXp
    }

    fun reset() {
        level = 0
        xp = 0
    }

    companion object {
        private const val PREFS_NAME = "player_progress"
        private const val KEY_LEVEL = "level"
        private const val KEY_XP = "xp"
        const val XP_PER_LEVEL = 100
        const val MAX_LEVEL = 99
        const val XP_PER_TASK = 20

        @Volatile private var instance: PlayerProgress? = null

        fun get(context: Context): PlayerProgress =
            instance ?: synchronized(this) {
                instance ?: PlayerProgress(context.applicationContext).also { instance = it }
            }
    }
}