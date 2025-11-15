package com.example.habitflow.ui.settings.skills

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habitflow.databinding.ItemSimpleTextBinding

class UnlockedSkillsAdapter(
    private var items: List<String>
) : RecyclerView.Adapter<UnlockedSkillsAdapter.SkillVH>() {

    inner class SkillVH(val binding: ItemSimpleTextBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSimpleTextBinding.inflate(inflater, parent, false)
        return SkillVH(binding)
    }

    override fun onBindViewHolder(holder: SkillVH, position: Int) {
        holder.binding.text1.text = items[position]
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }
}