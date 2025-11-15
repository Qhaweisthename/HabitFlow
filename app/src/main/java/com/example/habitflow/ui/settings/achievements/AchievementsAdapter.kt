package com.example.habitflow.ui.settings.achievements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habitflow.R
import com.example.habitflow.achievements.AchievementProgress
import com.example.habitflow.databinding.ItemAchievementBinding

class AchievementsAdapter(
    private var items: List<AchievementProgress>
) : RecyclerView.Adapter<AchievementsAdapter.AchievementVH>() {

    inner class AchievementVH(val binding: ItemAchievementBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementVH {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AchievementVH(binding)
    }

    override fun onBindViewHolder(holder: AchievementVH, position: Int) {
        val item = items[position]
        val ctx = holder.binding.root.context

        holder.binding.tvTitle.text = item.title
        holder.binding.tvDescription.text = item.description

        // Progress UI
        val max = if (item.target <= 0) 1 else item.target
        holder.binding.progressBar.max = max
        holder.binding.progressBar.progress = item.progress
        val percent = (item.progress * 100) / max
        holder.binding.tvProgress.text = "$percent%"

        // Unlocked vs locked styling
        if (item.unlocked) {
            holder.binding.root.setCardBackgroundColor(
                ContextCompat.getColor(ctx, R.color.section_purple)
            )
            holder.binding.tvTitle.setTextColor(ContextCompat.getColor(ctx, R.color.text_dark))
            holder.binding.tvDescription.setTextColor(ContextCompat.getColor(ctx, R.color.text_dark))
            holder.binding.tvProgress.setTextColor(ContextCompat.getColor(ctx, R.color.text_dark))
            holder.binding.progressBar.visibility = View.GONE
            holder.binding.badgeUnlocked.visibility = View.VISIBLE
            holder.binding.root.alpha = 1f
        } else {
            holder.binding.root.setCardBackgroundColor(
                ContextCompat.getColor(ctx, android.R.color.darker_gray)
            )
            holder.binding.tvTitle.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
            holder.binding.tvDescription.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
            holder.binding.tvProgress.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
            holder.binding.progressBar.visibility = View.VISIBLE
            holder.binding.badgeUnlocked.visibility = View.GONE
            holder.binding.root.alpha = 0.95f
        }
    }

    override fun getItemCount(): Int = items.size

    fun submit(newItems: List<AchievementProgress>) {
        items = newItems
        notifyDataSetChanged()
    }
}