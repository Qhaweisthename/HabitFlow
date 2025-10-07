package com.example.habitflow.ui.rewards.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habitflow.R
import com.example.habitflow.data.model.Skill
import com.google.android.material.button.MaterialButton

class SkillsShopAdapter(
    private var skills: List<Skill>,
    private var owned: Set<Int>,
    private val onBuy: (Skill) -> Unit
) : RecyclerView.Adapter<SkillsShopAdapter.SkillVH>() {

    inner class SkillVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvCost: TextView = itemView.findViewById(R.id.tvCost)
        val btnBuy: MaterialButton = itemView.findViewById(R.id.btnBuy)
        val tvOwned: TextView = itemView.findViewById(R.id.tvOwned)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_skill_card, parent, false)
        return SkillVH(view)
    }

    override fun onBindViewHolder(holder: SkillVH, position: Int) {
        val skill = skills[position]
        holder.tvName.text = skill.name
        holder.tvCost.text = "Cost: ${skill.cost}"

        val isOwned = owned.contains(skill.id)
        if (isOwned) {
            holder.btnBuy.visibility = View.GONE
            holder.tvOwned.visibility = View.VISIBLE
        } else {
            holder.btnBuy.visibility = View.VISIBLE
            holder.tvOwned.visibility = View.GONE
            holder.btnBuy.setOnClickListener { onBuy(skill) }
        }
    }

    override fun getItemCount(): Int = skills.size

    fun update(newSkills: List<Skill>, newOwned: Set<Int>) {
        this.skills = newSkills
        this.owned = newOwned
        notifyDataSetChanged()
    }
}
