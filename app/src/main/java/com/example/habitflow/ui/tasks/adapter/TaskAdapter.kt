package com.example.habitflow.ui.tasks.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.habitflow.data.model.Task
import com.example.habitflow.databinding.ItemTaskBinding
import com.example.habitflow.ui.progress.PlayerProgress

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskChecked: (Task) -> Unit,
    private val onTaskDeleted: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Detach listener before changing checked state to avoid feedback loop
        holder.binding.checkbox.setOnCheckedChangeListener(null)

        holder.binding.checkbox.text = task.name
        holder.binding.checkbox.isChecked = task.isDone
        updateStrikeThrough(holder.binding.checkbox, task.isDone)

        // Reattach listener
        holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (holder.adapterPosition == RecyclerView.NO_POSITION) return@setOnCheckedChangeListener
            task.isDone = isChecked
            updateStrikeThrough(holder.binding.checkbox, isChecked)

            // ✅ Award XP only when marking as done (keep XP here to avoid double-XP)
            if (isChecked) {
                val progress = PlayerProgress.get(holder.binding.root.context)
                progress.addExperience(PlayerProgress.XP_PER_TASK)
            }

            onTaskChecked(task) // Fragment handles achievements + coins + API update
        }

        holder.binding.btnDelete.setOnClickListener {
            if (holder.adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            onTaskDeleted(task)
        }
    }

    fun addTask(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    fun clearAll() {
        tasks.clear()
        notifyDataSetChanged()
    }

    private fun updateStrikeThrough(view: CheckBox, isChecked: Boolean) {
        if (isChecked) {
            view.paintFlags = view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            view.paintFlags = view.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount(): Int = tasks.size
}
