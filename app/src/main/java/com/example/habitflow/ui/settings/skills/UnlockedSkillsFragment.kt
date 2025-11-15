package com.example.habitflow.ui.settings.skills

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.room.Room
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.databinding.FragmentUnlockedSkillsBinding
import com.example.habitflow.ui.rewards.adapter.SkillsShopAdapter
import com.example.habitflow.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UnlockedSkillsFragment : Fragment() {
    private var _binding: FragmentUnlockedSkillsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnlockedSkillsBinding.inflate(inflater, container, false)
        binding.rvUnlockedSkills.layoutManager = GridLayoutManager(requireContext(), 3)
        loadSkills()
        return binding.root
    }

    private fun loadSkills() {
        val email = SessionManager(requireContext()).getUserSession() ?: return
        val db = Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java,
            "habitflow_db"
        ).fallbackToDestructiveMigration().build()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val ownedSkillIds = db.userSkillDao().getForUser(email).map { it.skillId }.toSet()
            val ownedSkills = if (ownedSkillIds.isEmpty()) emptyList()
            else db.skillDao().getByIds(ownedSkillIds.toList())

            withContext(Dispatchers.Main) {
                binding.rvUnlockedSkills.adapter = SkillsShopAdapter(
                    skills = ownedSkills,
                    owned = ownedSkillIds,
                    onBuy = { /* no-op on unlocked screen */ }
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}