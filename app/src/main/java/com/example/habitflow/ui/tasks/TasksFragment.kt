package com.example.habitflow.ui.tasks

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.habitflow.R
import com.example.habitflow.achievements.AchievementsRewards
import com.example.habitflow.achievements.AchievementsStore
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.data.model.Task
import com.example.habitflow.databinding.FragmentTasksBinding
import com.example.habitflow.network.RetrofitInstance
import com.example.habitflow.repository.TaskRepository
import com.example.habitflow.ui.tasks.adapter.TaskAdapter
import com.example.habitflow.util.NetworkUtils
import com.example.habitflow.util.SessionManager
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var viewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        taskAdapter = TaskAdapter(
            mutableListOf(),
            onTaskChecked = { task ->
                if (task.isDone) {
                    AchievementsStore.onTaskCompleted(requireContext())
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
                viewModel.updateTask(task)
                updateExperienceBar()
            },
            onTaskDeleted = { task ->
                viewModel.deleteTask(task)
            }
        )

        try {
            viewModel.fetchTasks()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = taskAdapter

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.updateTasks(tasks)
            updateProgressBars(tasks)
        }

        // Coins sync with DB
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val email = SessionManager(requireContext()).getUserSession()
            if (!email.isNullOrEmpty()) {
                val db = AppDatabase.getInstance(requireContext())
                val dbCoins = db.userDao().getCoins(email) ?: 100
                val current = viewModel.coins.value ?: 0
                val delta = dbCoins - current
                if (delta != 0) {
                    if (delta > 0) viewModel.addCoins(delta)
                    else viewModel.removeCoins(-delta)
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

        var pickedDate = Task.getTodayDate()
        tvPickedDate.text = pickedDate

        tvPickedDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dp = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    pickedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(calendar.time)
                    tvPickedDate.text = pickedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dp.show()
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

    private fun updateProgressBars(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            updateExperienceBar()
            binding.progressHealth.progress = 0
            binding.progressMana.progress = 0
            return
        }

        val done = tasks.count { it.isDone }
        val ratio = done.toFloat() / tasks.size

        updateExperienceBar()
        binding.progressHealth.progress = (ratio * 80).toInt()
        binding.progressMana.progress = (ratio * 60).toInt()
    }

    private fun updateExperienceBar() {
        val progress = com.example.habitflow.ui.progress.PlayerProgress.get(requireContext())
        val xp = progress.xp.coerceIn(
            0,
            com.example.habitflow.ui.progress.PlayerProgress.XP_PER_LEVEL
        )
        binding.progressExperience.progress = xp
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


// ------------------ DTOs ------------------

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


// ------------------ VIEWMODEL ------------------

class TaskViewModel : ViewModel() {

    private val appContext = com.example.habitflow.App.instance.applicationContext

    private val repo = TaskRepository(
        appContext,
        RetrofitInstance.api
    )

    private val _tasks = MutableLiveData<MutableList<Task>>(mutableListOf())
    val tasks: LiveData<MutableList<Task>> get() = _tasks

    private val _coins = MutableLiveData(300)
    val coins: LiveData<Int> get() = _coins

    fun fetchTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = "guest@habitflow.com"

            if (NetworkUtils.isOnline(appContext)) {
                try {
                    val response = RetrofitInstance.api.getTasks()
                    if (response.isSuccessful && response.body() != null) {
                        val apiTasks = response.body()!!.map { it.toUiTask() }

                        // Sync pending local tasks
                        repo.syncPending()

                        // Save remote tasks locally IF not already saved
                        apiTasks.forEach { repo.insertIfNotExists(it) }

                        // Load all tasks (merged local + remote)
                        val merged = repo.loadLocal(email)

                        _tasks.postValue(
                            merged.distinctBy {
                                it.remoteId ?: (it.name + it.date + it.userEmail)
                            }.toMutableList()
                        )

                        return@launch
                    }
                } catch (_: Exception) { }
            }

            // OFFLINE MODE
            val local = repo.loadLocal(email)

            _tasks.postValue(
                local.distinctBy {
                    it.remoteId ?: (it.name + it.date + it.userEmail)
                }.toMutableList()
            )
        }
    }


    fun newTask(
        name: String,
        date: String = Task.getTodayDate(),
        userEmail: String = "guest@habitflow.com"
    ): Task {
        return Task(
            id = 0,
            userEmail = userEmail,
            name = name,
            isDone = false,
            date = date
        )
    }

    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.addTask(task)

            if (!NetworkUtils.isOnline(appContext)) {
                val local = repo.loadLocal("guest@habitflow.com")
                _tasks.postValue(local.toMutableList())
                return@launch
            }

            fetchTasks()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateTask(task)
            fetchTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteTask(task)
            fetchTasks()
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.clearAll("guest@habitflow.com")
            _tasks.postValue(mutableListOf())
        }
    }

    fun addCoins(amount: Int) {
        _coins.postValue((_coins.value ?: 0) + amount)
    }

    fun removeCoins(amount: Int) {
        _coins.postValue((_coins.value ?: 0) - amount)
    }
}


// ------------------ MAPPING ------------------

fun ApiTask.toUiTask(): Task =
    Task(
        id = 0,
        userEmail = "guest@habitflow.com",
        name = this.name,
        isDone = this.isDone,
        date = this.date,
        remoteId = this._id
    )

fun Task.toCreateRequest(): CreateTaskRequest =
    CreateTaskRequest(name, isDone, date)

fun Task.toUpdateRequest(): UpdateTaskRequest =
    UpdateTaskRequest(name, isDone, date)
