package com.example.habitflow

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitflow.adapter.TaskAdapter
import com.example.habitflow.databinding.FragmentTasksBinding
import com.example.habitflow.model.Task
import java.text.SimpleDateFormat
import java.util.*

class TasksFragment : Fragment() {
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var viewModel: TaskViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,

        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]


        taskAdapter = TaskAdapter(
            mutableListOf(),
            onTaskChecked = { task -> viewModel.updateTask(task) },
            onTaskDeleted = { task -> viewModel.deleteTask(task) }
        )
        try {
            viewModel.fetchTasks()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = taskAdapter

        // Observe tasks
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks != null) {
                taskAdapter.updateTasks(tasks)
                updateProgressBars(tasks)
            }
        }

        // Clear all
        binding.btnClearAll.setOnClickListener {
            viewModel.clearAll()
        }

        // ✅ FAB add task with date picker
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        return binding.root
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)
        val tvPickedDate = dialogView.findViewById<TextView>(R.id.tvPickedDate)

        var pickedDate = Task.getTodayDate() // default today
        tvPickedDate.text = pickedDate

        tvPickedDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    calendar.set(year, month, dayOfMonth)
                    pickedDate = format.format(calendar.time)
                    tvPickedDate.text = pickedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etTaskName.text.toString()
                if (name.isNotBlank()) {
                    viewModel.addTask(viewModel.newTask(name, pickedDate))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun addTask(name: String) {
        if (name.isNotBlank()) {
            viewModel.addTask(viewModel.newTask(name))
        }
    }


    private fun updateProgressBars(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            binding.progressExperience.progress = 0
            binding.progressHealth.progress = 0
            binding.progressMana.progress = 0
            return
        }

        val completed = tasks.count { it.isDone }
        val ratio = completed.toFloat() / tasks.size

        binding.progressExperience.progress = (ratio * 100).toInt()
        binding.progressHealth.progress = (ratio * 80).toInt()
        binding.progressMana.progress = (ratio * 60).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
