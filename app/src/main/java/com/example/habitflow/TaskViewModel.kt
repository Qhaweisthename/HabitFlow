package com.example.habitflow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.model.Task
import com.example.habitflow.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private val _tasks = MutableLiveData<MutableList<Task>>(mutableListOf())
    val tasks: LiveData<MutableList<Task>> get() = _tasks

    private val _coins = MutableLiveData(300)
    val coins: LiveData<Int> get() = _coins

    /** --- Fetch tasks from API --- **/
    fun fetchTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getTasks()
                if (response.isSuccessful) {
                    val apiTasks = response.body() ?: emptyList<ApiTask>()
                    val mapped = apiTasks.map { it.toUiTask() }.toMutableList()
                    _tasks.postValue(mapped)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** --- Create new local Task --- **/
    fun newTask(name: String, date: String = Task.getTodayDate()): Task {
        return Task(
            id = 0, // not used for Mongo
            name = name,
            isDone = false,
            date = date
        )
    }

    /** --- Add Task to API and local list --- **/
    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = task.toCreateRequest()
                val response = RetrofitInstance.api.addTask(request)
                if (response.isSuccessful) {
                    // If API doesn’t return new task data, just re-fetch from server
                    val newTask = response.body()?.toUiTask()
                    if (newTask != null) {
                        val current = _tasks.value ?: mutableListOf()
                        current.add(newTask)
                        _tasks.postValue(current)
                    } else {
                        fetchTasks() // 🔁 refresh the full list from the API
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    /** --- Update Task on API --- **/
    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val remoteId = task.remoteId ?: return@launch
                val request = task.toUpdateRequest()
                RetrofitInstance.api.updateTask(remoteId, request)
                val current = _tasks.value ?: return@launch
                val index = current.indexOfFirst { it.remoteId == remoteId }
                if (index != -1) {
                    val wasDone = current[index].isDone
                    current[index] = task
                    _tasks.postValue(current)
                    if (!wasDone && task.isDone) addCoins(10)
                    else if (wasDone && !task.isDone) removeCoins(10)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** --- Delete Task from API --- **/
    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val remoteId = task.remoteId ?: return@launch
                RetrofitInstance.api.deleteTask(remoteId)
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
                        task.remoteId?.let { RetrofitInstance.api.deleteTask(it) }
                    } catch (_: Exception) { }
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
fun ApiTask.toUiTask(): Task {
    return Task(
        id = 0,
        name = this.name,
        isDone = this.isDone,
        date = this.date,
        remoteId = this._id
    )
}

// Converts UI Task -> Create API Request
fun Task.toCreateRequest(): CreateTaskRequest {
    return CreateTaskRequest(
        name = this.name,
        isDone = this.isDone,
        date = this.date
    )
}

// Converts UI Task -> Update API Request
fun Task.toUpdateRequest(): UpdateTaskRequest {
    return UpdateTaskRequest(
        name = this.name,
        isDone = this.isDone,
        date = this.date
    )
}
