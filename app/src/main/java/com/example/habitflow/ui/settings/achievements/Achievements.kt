package com.example.habitflow.achievements

import android.content.Context
import android.os.SystemClock
import com.example.habitflow.ui.progress.PlayerProgress
import com.example.habitflow.util.AppUsageTracker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ---------- Types ----------
enum class AchievementType { TOTAL_TASKS, WEEK_TASKS, STREAK_DAYS, LEVEL_REACHED, TOTAL_TIME_MS, COINS_EARNED }

data class AchievementDef(
    val id: Int,
    val type: AchievementType,
    val title: String,
    val description: (current: Int, target: Int) -> String,
    val target: Int,
    val unitLabel: String = "",
    val rewardCoins: Int = 0, // <-- NEW: coins awarded on unlock
    val isHiddenUntilUnlock: Boolean = false
)

data class AchievementProgress(
    val id: Int,
    val title: String,
    val description: String,
    val progress: Int,
    val target: Int,
    val unlocked: Boolean
)

// ---------- Store (counters + credited set) ----------
object AchievementsStore {
    private const val PREF = "achievements_store"
    private const val KEY_TOTAL_TASKS = "total_tasks"
    private const val KEY_WEEK_TASKS = "week_tasks"
    private const val KEY_WEEK_YEAR = "week_year"
    private const val KEY_WEEK_NO = "week_no"
    private const val KEY_STREAK_DAYS = "streak_days"
    private const val KEY_LAST_COMPLETION_DATE = "last_completion_date"
    private const val KEY_COINS_EARNED = "coins_earned"
    private const val KEY_CREDITED_IDS = "credited_ids" // Set<String> of achievement IDs

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun onTaskCompleted(ctx: Context, whenMillis: Long = SystemClock.elapsedRealtime()) {
        val p = prefs(ctx)
        val total = p.getInt(KEY_TOTAL_TASKS, 0) + 1

        val now = Calendar.getInstance()
        val weekYearNow = now.get(Calendar.YEAR)
        val weekNow = now.get(Calendar.WEEK_OF_YEAR)
        val storedWeekYear = p.getInt(KEY_WEEK_YEAR, -1)
        val storedWeek = p.getInt(KEY_WEEK_NO, -1)
        val weekCount = if (storedWeekYear == weekYearNow && storedWeek == weekNow) {
            p.getInt(KEY_WEEK_TASKS, 0) + 1
        } else 1

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
        val lastDate = p.getString(KEY_LAST_COMPLETION_DATE, null)
        val streak = when {
            lastDate == null -> 1
            lastDate == todayStr -> p.getInt(KEY_STREAK_DAYS, 0)
            isYesterday(lastDate) -> p.getInt(KEY_STREAK_DAYS, 0) + 1
            else -> 1
        }

        p.edit()
            .putInt(KEY_TOTAL_TASKS, total)
            .putInt(KEY_WEEK_TASKS, weekCount)
            .putInt(KEY_WEEK_YEAR, weekYearNow)
            .putInt(KEY_WEEK_NO, weekNow)
            .putInt(KEY_STREAK_DAYS, streak)
            .putString(KEY_LAST_COMPLETION_DATE, todayStr)
            .apply()
    }

    fun addCoinsEarned(ctx: Context, delta: Int) {
        if (delta <= 0) return
        val p = prefs(ctx)
        p.edit().putInt(KEY_COINS_EARNED, p.getInt(KEY_COINS_EARNED, 0) + delta).apply()
    }

    fun getTotalTasks(ctx: Context) = prefs(ctx).getInt(KEY_TOTAL_TASKS, 0)
    fun getWeekTasks(ctx: Context): Pair<Int, Pair<Int, Int>> {
        val p = prefs(ctx)
        return p.getInt(KEY_WEEK_TASKS, 0) to (p.getInt(KEY_WEEK_YEAR, -1) to p.getInt(KEY_WEEK_NO, -1))
    }
    fun getStreakDays(ctx: Context) = prefs(ctx).getInt(KEY_STREAK_DAYS, 0)
    fun getCoinsEarned(ctx: Context) = prefs(ctx).getInt(KEY_COINS_EARNED, 0)

    // credited ids (to not award twice)
    fun getCreditedIds(ctx: Context): MutableSet<String> =
        HashSet(prefs(ctx).getStringSet(KEY_CREDITED_IDS, emptySet()) ?: emptySet())

    fun markCredited(ctx: Context, ids: Collection<Int>) {
        val s = getCreditedIds(ctx)
        ids.forEach { s.add(it.toString()) }
        prefs(ctx).edit().putStringSet(KEY_CREDITED_IDS, s).apply()
    }

    private fun isYesterday(dateStr: String): Boolean {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val last = Calendar.getInstance().apply { time = fmt.parse(dateStr) ?: return false }
        val y = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return last.get(Calendar.YEAR) == y.get(Calendar.YEAR) &&
                last.get(Calendar.DAY_OF_YEAR) == y.get(Calendar.DAY_OF_YEAR)
    }
}

// ---------- Catalog (with rewards!) ----------
object AchievementsCatalog {
    val all: List<AchievementDef> = listOf(
        // Total tasks
        AchievementDef(1,  AchievementType.TOTAL_TASKS, "First Steps",        { c,t -> "Complete $t tasks ($c/$t)" },   1,   "tasks", rewardCoins = 10),
        AchievementDef(2,  AchievementType.TOTAL_TASKS, "Getting Things Done",{ c,t -> "Complete $t tasks ($c/$t)" },   10,  "tasks", rewardCoins = 20),
        AchievementDef(3,  AchievementType.TOTAL_TASKS, "Consistency Champ",  { c,t -> "Complete $t tasks ($c/$t)" },   30,  "tasks", rewardCoins = 50),
        AchievementDef(4,  AchievementType.TOTAL_TASKS, "Task Master",        { c,t -> "Complete $t tasks ($c/$t)" },   100, "tasks", rewardCoins = 150),

        // Weekly completions
        AchievementDef(10, AchievementType.WEEK_TASKS,  "Weekly Warrior",     { c,t -> "Complete $t this week ($c/$t)" }, 7,  "tasks", rewardCoins = 25),
        AchievementDef(11, AchievementType.WEEK_TASKS,  "Week Crusher",       { c,t -> "Complete $t this week ($c/$t)" }, 20, "tasks", rewardCoins = 60),

        // Streaks
        AchievementDef(20, AchievementType.STREAK_DAYS, "Streak Starter",     { c,t -> "Finish $t days in a row ($c/$t)" }, 3,  "days", rewardCoins = 20),
        AchievementDef(21, AchievementType.STREAK_DAYS, "Streak Keeper",      { c,t -> "Finish $t days in a row ($c/$t)" }, 7,  "days", rewardCoins = 60),
        AchievementDef(22, AchievementType.STREAK_DAYS, "Unstoppable",        { c,t -> "Finish $t days in a row ($c/$t)" }, 30, "days", rewardCoins = 200),

        // Level
        AchievementDef(30, AchievementType.LEVEL_REACHED,"Level Up!",         { c,t -> "Reach Level $t (now L$c)" },     5,  rewardCoins = 25),
        AchievementDef(31, AchievementType.LEVEL_REACHED,"Adventurer",        { c,t -> "Reach Level $t (now L$c)" },     10, rewardCoins = 75),

        // Time in app
        AchievementDef(40, AchievementType.TOTAL_TIME_MS,"Getting Warmed Up", { c,t -> "Spend ${t/60000}m (${c/60000}m/${t/60000}m)" }, 30*60*1000, rewardCoins = 10),
        AchievementDef(41, AchievementType.TOTAL_TIME_MS,"Here to Stay",      { c,t -> "Spend ${t/3600000}h (${c/3600000}h/${t/3600000}h)" }, 3*60*60*1000, rewardCoins = 50),

        // Coins (lifetime)
        AchievementDef(50, AchievementType.COINS_EARNED, "Shopper",           { c,t -> "Earn or spend $t coins ($c/$t)" },100,  rewardCoins = 20),
        AchievementDef(51, AchievementType.COINS_EARNED, "High Roller",       { c,t -> "Earn or spend $t coins ($c/$t)" },1000, rewardCoins = 100),
    )
}

// ---------- Engine reads current values ----------
object AchievementsEngine {
    fun buildProgress(ctx: Context): List<AchievementProgress> {
        val totalTasks = AchievementsStore.getTotalTasks(ctx)
        val (weekTasks, _) = AchievementsStore.getWeekTasks(ctx)
        val streakDays = AchievementsStore.getStreakDays(ctx)
        val level = PlayerProgress.get(ctx).level
        val timeMs = AppUsageTracker.getCurrentTotalMs(ctx)
        val coinsLifetime = AchievementsStore.getCoinsEarned(ctx)

        fun currentFor(t: AchievementType) = when (t) {
            AchievementType.TOTAL_TASKS   -> totalTasks
            AchievementType.WEEK_TASKS    -> weekTasks
            AchievementType.STREAK_DAYS   -> streakDays
            AchievementType.LEVEL_REACHED -> level
            AchievementType.TOTAL_TIME_MS -> timeMs.toInt()
            AchievementType.COINS_EARNED  -> coinsLifetime
        }

        return AchievementsCatalog.all.map { def ->
            val cur = currentFor(def.type).coerceAtLeast(0)
            val unlocked = cur >= def.target
            AchievementProgress(
                id = def.id,
                title = def.title,
                description = def.description(cur, def.target),
                progress = cur.coerceAtMost(def.target),
                target = def.target,
                unlocked = unlocked
            )
        }
    }
}

// ---------- Rewards helper (detect new unlocks, credit once, grant coins) ----------
object AchievementsRewards {

    /** Returns list of (progress, coinsGrantedForThatAchievement). Also calls grantCoins(total) once. */
    fun processUnlocks(ctx: Context, grantCoins: (Int) -> Unit): List<Pair<AchievementProgress, Int>> {
        val rows = AchievementsEngine.buildProgress(ctx)
        val credited = AchievementsStore.getCreditedIds(ctx)
        val newly = rows.filter { it.unlocked && !credited.contains(it.id.toString()) }
        if (newly.isEmpty()) return emptyList()

        val rewards = newly.map { row ->
            val coins = AchievementsCatalog.all.firstOrNull { it.id == row.id }?.rewardCoins ?: 0
            row to coins
        }
        val total = rewards.sumOf { it.second }

        // Grant and mark
        if (total > 0) {
            grantCoins(total)
            AchievementsStore.addCoinsEarned(ctx, total) // optional lifetime stat
        }
        AchievementsStore.markCredited(ctx, newly.map { it.id })

        return rewards
    }
}
