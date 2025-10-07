package com.example.habitflow.ui.tasks

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.habitflow.R
import com.example.habitflow.achievements.AchievementsRewards
import com.example.habitflow.achievements.AchievementsStore
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.databinding.FragmentTasksBinding
import com.example.habitflow.ui.tasks.adapter.TaskAdapter
import com.example.habitflow.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
            onTaskChecked = { task ->
                // 🔹 Record completion + grant achievement coin rewards (only when marking as done)
                if (task.isDone) {
                    // Count toward totals / week / streak
                    AchievementsStore.onTaskCompleted(requireContext())

                    // Credit any newly unlocked achievements and add coins via ViewModel
                    val unlocked = AchievementsRewards.processUnlocks(requireContext()) { bonusCoins ->
                        viewModel.addCoins(bonusCoins)
                    }
                    if (unlocked.isNotEmpty()) {
                        val totalBonus = unlocked.sumOf { it.second }
                        Toast.makeText(
                            requireContext(),
                            "Achievement unlocked! +$totalBonus coins",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // 🔹 Persist task change and refresh bars
                viewModel.updateTask(task)
                updateExperienceBar()
            },
            onTaskDeleted = { task -> viewModel.deleteTask(task) }
        )

        try {
            viewModel.fetchTasks()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = taskAdapter

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks != null) {
                taskAdapter.updateTasks(tasks)
                updateProgressBars(tasks)
            }
        }

        // Initialize coins from DB and keep DB in sync with LiveData
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val email = SessionManager(requireContext()).getUserSession()
            if (!email.isNullOrEmpty()) {
                val db = Room.databaseBuilder(
                    requireContext().applicationContext,
                    AppDatabase::class.java,
                    "habitflow_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                val dbCoins = db.userDao().getCoins(email) ?: 100
                val current = viewModel.coins.value ?: 0
                val delta = dbCoins - current
                if (delta != 0) {
                    if (delta > 0) viewModel.addCoins(delta) else viewModel.removeCoins(-delta)
                }

                withContext(Dispatchers.Main) {
                    viewModel.coins.observe(viewLifecycleOwner) { coins ->
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            db.userDao().updateCoins(email, coins)
                        }
                    }
                }
            }
        }

        binding.btnClearAll.setOnClickListener { viewModel.clearAll() }
        binding.fabAddTask.setOnClickListener { showAddTaskDialog() }

        return binding.root
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)
        val tvPickedDate = dialogView.findViewById<TextView>(R.id.tvPickedDate)

        var pickedDate = com.example.habitflow.data.model.Task.getTodayDate() // default today
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

    private fun updateProgressBars(tasks: List<com.example.habitflow.data.model.Task>) {
        if (tasks.isEmpty()) {
            updateExperienceBar()
            binding.progressHealth.progress = 0
            binding.progressMana.progress = 0
            return
        }

        val completed = tasks.count { it.isDone }
        val ratio = completed.toFloat() / tasks.size

        // Experience reflects current XP in the active level (0..100)
        updateExperienceBar()
        binding.progressHealth.progress = (ratio * 80).toInt()
        binding.progressMana.progress = (ratio * 60).toInt()
    }

    private fun updateExperienceBar() {
        val progress = com.example.habitflow.ui.progress.PlayerProgress.get(requireContext())
        val xp = progress.xp.coerceIn(0, com.example.habitflow.ui.progress.PlayerProgress.XP_PER_LEVEL)
        binding.progressExperience.progress = xp
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// --- DTOs for Mongo API ---
data class ApiTask(
    val _id: String?,
    val name: String,
    val isDone: Boolean,
    val date: String
)

data class CreateTaskRequest(
    val name: String,
    val isDone: Boolean,
    val date: String
)

data class UpdateTaskRequest(
    val name: String,
    val isDone: Boolean,
    val date: String
)

class TaskViewModel : ViewModel() {
    private val _tasks = MutableLiveData<MutableList<com.example.habitflow.data.model.Task>>(mutableListOf())
    val tasks: LiveData<MutableList<com.example.habitflow.data.model.Task>> get() = _tasks

    private val _coins = MutableLiveData(300)
    val coins: LiveData<Int> get() = _coins

    /** --- Fetch tasks from API --- **/
    fun fetchTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                println("🟢 Fetching tasks from API...")
                val response = com.example.habitflow.network.RetrofitInstance.api.getTasks()
                if (response.isSuccessful) {
                    val apiTasks = response.body() ?: emptyList<ApiTask>()
                    println("✅ Received ${apiTasks.size} tasks from API.")
                    val mapped = apiTasks.map { it.toUiTask() }.toMutableList()
                    _tasks.postValue(mapped)
                } else {
                    println("⚠️ API Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("🚨 Network or conversion error: ${e.message}")
                _tasks.postValue(
                    mutableListOf(
                        com.example.habitflow.data.model.Task(
                            id = 1,
                            name = "Sample Task (offline)",
                            isDone = false,
                            date = com.example.habitflow.data.model.Task.getTodayDate()
                        )
                    )
                )
            }
        }
    }

    /** --- Create new local Task --- **/
    fun newTask(
        name: String,
        date: String = com.example.habitflow.data.model.Task.getTodayDate(),
        userEmail: String = "guest@habitflow.com"
    ): com.example.habitflow.data.model.Task {
        return com.example.habitflow.data.model.Task(
            id = 0,
            userEmail = userEmail,
            name = name,
            isDone = false,
            date = date
        )
    }

    /** --- Add Task to API and local list --- **/
    fun addTask(task: com.example.habitflow.data.model.Task) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = task.toCreateRequest()
                val response = com.example.habitflow.network.RetrofitInstance.api.addTask(request)
                if (response.isSuccessful) {
                    val newTask = response.body()?.toUiTask()
                    if (newTask != null) {
                        val current = _tasks.value ?: mutableListOf()
                        current.add(newTask)
                        _tasks.postValue(current)
                    } else {
                        fetchTasks() // refresh the full list from the API
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** --- Update Task on API --- **/
    fun updateTask(task: com.example.habitflow.data.model.Task) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val remoteId = task.remoteId ?: return@launch
                val request = task.toUpdateRequest()
                com.example.habitflow.network.RetrofitInstance.api.updateTask(remoteId, request)

                val current = _tasks.value ?: return@launch
                val index = current.indexOfFirst { it.remoteId == remoteId }
                if (index != -1) {
                    val wasDone = current[index].isDone
                    current[index] = task
                    _tasks.postValue(current)

                    // Coins +/- when toggling done/undone
                    if (!wasDone && task.isDone) addCoins(10)
                    else if (wasDone && !task.isDone) removeCoins(10)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** --- Delete Task from API --- **/
    fun deleteTask(task: com.example.habitflow.data.model.Task) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val remoteId = task.remoteId ?: return@launch
                com.example.habitflow.network.RetrofitInstance.api.deleteTask(remoteId)
                val current = _tasks.value ?: return@launch
                current.removeAll { it.remoteId == remoteId }
                _tasks.postValue(current)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** --- Clear All Tasks --- **/
    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val current = _tasks.value ?: mutableListOf()
                for (task in current) {
                    try {
                        task.remoteId?.let { com.example.habitflow.network.RetrofitInstance.api.deleteTask(it) }
                    } catch (_: Exception) {
                        // ignore individual failures and continue
                    }
                }
                _tasks.postValue(mutableListOf())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** --- Coins Logic --- **/
    fun addCoins(amount: Int) {
        _coins.postValue((_coins.value ?: 0) + amount)
    }

    fun removeCoins(amount: Int) {
        _coins.postValue((_coins.value ?: 0) - amount)
    }
}

/** --- Mapping Extensions (bottom of file) --- **/

// Converts API DTO -> UI model
fun ApiTask.toUiTask(): com.example.habitflow.data.model.Task {
    return com.example.habitflow.data.model.Task(
        id = 0,
        userEmail = "guest@habitflow.com", // or get from logged-in user
        name = this.name,
        isDone = this.isDone,
        date = this.date,
        remoteId = this._id
    )
}

// Converts UI Task -> Create API Request
fun com.example.habitflow.data.model.Task.toCreateRequest(): CreateTaskRequest {
    return CreateTaskRequest(
        name = this.name,
        isDone = this.isDone,
        date = this.date
    )
}

// Converts UI Task -> Update API Request
fun com.example.habitflow.data.model.Task.toUpdateRequest(): UpdateTaskRequest {
    return UpdateTaskRequest(
        name = this.name,
        isDone = this.isDone,
        date = this.date
    )
}
