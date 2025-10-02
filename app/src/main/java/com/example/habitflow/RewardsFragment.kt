package com.example.habitflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitflow.adapter.RewardAdapter
import com.example.habitflow.databinding.FragmentRewardsBinding
import com.example.habitflow.model.Reward

class RewardsFragment : Fragment() {
    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TaskViewModel
    private lateinit var rewardAdapter: RewardAdapter

    private val rewards = mutableListOf(
        Reward(1, "5 min Break", 50),
        Reward(2, "Watch a Video", 100),
        Reward(3, "Snack Time", 150),
        Reward(4, "Play Game 15 min", 200)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        binding.rvRewards.layoutManager = LinearLayoutManager(requireContext())
        rewardAdapter = RewardAdapter(rewards) { reward ->
            claimReward(reward)
        }
        binding.rvRewards.adapter = rewardAdapter

        // ✅ Observe coins in real-time
        viewModel.coins.observe(viewLifecycleOwner) { coins ->
            binding.tvCoins.text = "Coins: $coins"
        }

        return binding.root
    }

    private fun claimReward(reward: Reward) {
        val coins = viewModel.coins.value ?: 0
        if (coins >= reward.cost) {
            viewModel.removeCoins(reward.cost)
            reward.claimed = true
            rewardAdapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Claimed: ${reward.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Not enough coins!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
