package com.example.habitflow.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitflow.TaskViewModel
import com.example.habitflow.adapter.RewardAdapter
import com.example.habitflow.databinding.FragmentRewardsBinding
import com.example.habitflow.model.Reward

class RewardsFragment : Fragment() {
    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TaskViewModel
    private lateinit var rewardAdapter: RewardAdapter

    private val userEmail = "guest@habitflow.com" // later replace with logged-in user

    private val rewards = mutableListOf(
        Reward(id = 1, userEmail = userEmail, name = "5 min Break", cost = 50),
        Reward(id = 2, userEmail = userEmail, name = "Watch a Video", cost = 100),
        Reward(id = 3, userEmail = userEmail, name = "Snack Time", cost = 150),
        Reward(id = 4, userEmail = userEmail, name = "Play Game 15 min", cost = 200)
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        binding.rvRewards.layoutManager = LinearLayoutManager(requireContext())
        rewardAdapter = RewardAdapter(rewards) { reward -> claimReward(reward) }
        binding.rvRewards.adapter = rewardAdapter

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