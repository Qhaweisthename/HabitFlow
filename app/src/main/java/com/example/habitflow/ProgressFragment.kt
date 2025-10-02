package com.example.habitflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.habitflow.databinding.FragmentProgressBinding
import java.text.SimpleDateFormat
import java.util.*

class ProgressFragment : Fragment() {
    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        // Set today’s date
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(Date())

        // Observe tasks from ViewModel
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            val total = tasks.size
            val completed = tasks.count { it.isDone }

            binding.tvProgress.text = "$completed / $total"
            binding.tvMessage.text = when {
                total == 0 -> "No tasks yet"
                completed == 0 -> "Let’s get started!"
                completed == total -> "All tasks completed 🎉"
                else -> "Keep going, you’re making progress!"
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
