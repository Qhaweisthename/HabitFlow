package com.example.habitflow.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.habitflow.databinding.FragmentStatsBinding
import com.example.habitflow.ui.tasks.TaskViewModel
import com.example.habitflow.ui.progress.PlayerProgress
import kotlin.math.roundToInt
import com.example.habitflow.achievements.AchievementsStore
import com.example.habitflow.achievements.AchievementsEngine
import com.example.habitflow.util.AppUsageTracker

class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskViewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        // Observed stats from the live task list (session/contextual)
        taskViewModel.tasks.observe(viewLifecycleOwner) { list ->
            val tasks = list ?: emptyList()
            val completed = tasks.count { it.isDone }
            val ratio = if (tasks.isEmpty()) 0f else completed.toFloat() / tasks.size

            val hp = (ratio * 80).roundToInt().coerceIn(0, 100)
            val mana = (ratio * 60).roundToInt().coerceIn(0, 100)

            binding.tvTasksCompleted.text = "Tasks completed (current list): $completed"
            binding.tvHp.text = "HP: $hp/100"
            binding.tvMana.text = "Mana: $mana/100"
        }

        // Exact XP + Level from PlayerProgress
        val progress = PlayerProgress.get(requireContext())
        binding.tvExactXp.text = "XP: ${progress.xp} / ${PlayerProgress.XP_PER_LEVEL}"
        binding.tvLevel.text = "Level: ${progress.level}"

        // Lifetime stats (match achievements’ tracking)
        refreshLifetimeStats()


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // refresh lifetime counters when returning
        refreshLifetimeStats()

    }

    private fun refreshLifetimeStats() {
        // 🔹 Lifetime tasks completed — pulled from AchievementsStore (same source the achievements use)
        val lifetimeTasks = AchievementsStore.getTotalTasks(requireContext())
        binding.tvTotalTasksCompleted.text = "Total tasks completed: $lifetimeTasks"

        // 🔹 Total achievements unlocked — computed from current progress
        val unlockedCount = AchievementsEngine.buildProgress(requireContext()).count { it.unlocked }
        binding.tvAchievementsUnlocked.text = "Achievements unlocked: $unlockedCount"
    }

    private fun formatMs(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) "${hours}h ${minutes}m ${seconds}s"
        else "${minutes}m ${seconds}s"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
