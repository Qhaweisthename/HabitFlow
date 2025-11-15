package com.example.habitflow.ui.rewards.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habitflow.data.model.Reward
import com.example.habitflow.databinding.ItemRewardBinding

class RewardAdapter(
    private val rewards: MutableList<Reward>,
    private val onClaimClicked: (Reward) -> Unit
) : RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {

    inner class RewardViewHolder(val binding: ItemRewardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val binding = ItemRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RewardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val reward = rewards[position]

        holder.binding.tvRewardName.text = reward.name
        holder.binding.tvRewardCost.text = "${reward.cost} coins"

        holder.binding.btnClaim.isEnabled = !reward.claimed
        holder.binding.btnClaim.text = if (reward.claimed) "Claimed" else "Claim"

        holder.binding.btnClaim.setOnClickListener {
            if (!reward.claimed) {
                onClaimClicked(reward)
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = rewards.size

    fun updateRewards(newRewards: List<Reward>) {
        rewards.clear()
        rewards.addAll(newRewards)
        notifyDataSetChanged()
    }
}