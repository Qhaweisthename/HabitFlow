package com.example.habitflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.habitflow.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isEmpty()) {
                binding.tvTasksForDay.text = "No tasks yet."
                return@observe
            }

            // Group tasks by date
            val grouped = tasks.groupBy { it.date }
            val builder = StringBuilder()

            grouped.forEach { (date, list) ->
                builder.append("📅 $date\n")
                list.forEach { task ->
                    builder.append("   - ${task.name}${if (task.isDone) " ✅" else ""}\n")
                }
                builder.append("\n")
            }

            binding.tvTasksForDay.text = builder.toString()
        }

        return binding.root
    }

    private fun showTasksForDate(date: String) {
        val tasks = viewModel.tasks.value?.filter { it.date == date } ?: emptyList()
        val completed = tasks.count { it.isDone }
        val total = tasks.size

        binding.tvTasksOnDate.text = if (total == 0) {
            "No tasks for this date"
        } else {
            "Tasks: $completed / $total completed"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
