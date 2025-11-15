package com.example.habitflow.ui.rewards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.room.Room
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.data.model.Skill
import com.example.habitflow.data.model.UserSkill
import com.example.habitflow.databinding.FragmentRewardsBinding
import com.example.habitflow.ui.progress.PlayerProgress
import com.example.habitflow.ui.rewards.adapter.SkillsShopAdapter // <-- IMPORTANT import
import com.example.habitflow.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RewardsFragment : Fragment() {
    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        setupShop()
        return binding.root
    }

    private fun setupShop() {
        val email = SessionManager(requireContext()).getUserSession() ?: return
        val db = Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java,
            "habitflow_db"
        ).fallbackToDestructiveMigration().build()

        val level = PlayerProgress.get(requireContext()).level
        binding.rvShop.layoutManager = GridLayoutManager(requireContext(), 3)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            if (db.skillDao().getCount() == 0) {
                val seed = (1..15).map {
                    Skill(name = "Skill $it", cost = 20 * it, minLevel = ((it - 1) / 3) + 1)
                }
                db.skillDao().insertAll(seed)
            }

            val coins = db.userDao().getCoins(email) ?: 100
            val available = db.skillDao().getUnlockedForLevel(level)
            val ownedIds = db.userSkillDao().getForUser(email).map { it.skillId }.toSet()

            withContext(Dispatchers.Main) {
                binding.tvCoins.text = "Coins: $coins"
                binding.rvShop.adapter = SkillsShopAdapter(
                    skills = available,
                    owned = ownedIds,
                    onBuy = { skill: Skill ->  // <-- specify type to satisfy inference
                        purchaseSkill(db, email, skill)
                    }
                )
            }
        }
    }

    private fun purchaseSkill(db: AppDatabase, email: String, skill: Skill) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val currentCoins = db.userDao().getCoins(email) ?: 0
            if (currentCoins < skill.cost) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Not enough coins!", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            db.userDao().updateCoins(email, currentCoins - skill.cost)
            db.userSkillDao().insert(UserSkill(userEmail = email, skillId = skill.id))

            val level = PlayerProgress.get(requireContext()).level
            val updatedCoins = db.userDao().getCoins(email) ?: 0
            val updatedOwned = db.userSkillDao().getForUser(email).map { it.skillId }.toSet()
            val refreshedSkills = db.skillDao().getUnlockedForLevel(level)

            withContext(Dispatchers.Main) {
                binding.tvCoins.text = "Coins: $updatedCoins"
                (binding.rvShop.adapter as? SkillsShopAdapter)?.update(
                    newSkills = refreshedSkills,
                    newOwned = updatedOwned
                )
                Toast.makeText(requireContext(), "Purchased: ${skill.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
