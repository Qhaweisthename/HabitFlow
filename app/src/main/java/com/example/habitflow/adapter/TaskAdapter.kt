package com.example.habitflow.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habitflow.databinding.ItemTaskBinding
import com.example.habitflow.model.Task

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
            onTaskChecked(task) // ViewModel will emit new list; observer will refresh adapter
        }

        holder.binding.btnDelete.setOnClickListener {
            if (holder.adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            onTaskDeleted(task) // Let ViewModel delete; observer will refresh adapter
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

    private fun updateStrikeThrough(view: android.widget.CheckBox, isChecked: Boolean) {
        if (isChecked) {
            view.paintFlags = view.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            view.paintFlags = view.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount(): Int = tasks.size
}
