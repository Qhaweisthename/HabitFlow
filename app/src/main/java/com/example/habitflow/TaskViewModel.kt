package com.example.habitflow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.habitflow.model.Task

class TaskViewModel : ViewModel() {
    private val _tasks = MutableLiveData<MutableList<Task>>(mutableListOf())
    val tasks: LiveData<MutableList<Task>> get() = _tasks

    private val _coins = MutableLiveData(300)
    val coins: LiveData<Int> get() = _coins

    private var nextId = 1  // auto-increment ID

    /** --- Task creation --- **/
    fun newTask(name: String, date: String = Task.getTodayDate()): Task {
        return Task(
            id = nextId++,
            name = name,
            isDone = false,
            date = date
        )
    }

    fun addTask(task: Task) {
        val current = _tasks.value ?: mutableListOf()
        current.add(task)
        _tasks.value = current
    }

    fun updateTask(task: Task) {
        val current = _tasks.value ?: return
        val index = current.indexOfFirst { it.id == task.id }
        if (index != -1) {
            val wasDone = current[index].isDone
            current[index] = task
            _tasks.value = current

            // Coins logic when completing/unchecking tasks
            if (!wasDone && task.isDone) addCoins(10)
            else if (wasDone && !task.isDone) removeCoins(10)
        }
    }

    fun deleteTask(task: Task) {
        val current = _tasks.value ?: return
        current.remove(task)
        _tasks.value = current
    }

    fun clearAll() {
        _tasks.value = mutableListOf()
    }

    /** --- Coins logic --- **/
     fun addCoins(amount: Int) {
        _coins.value = (_coins.value ?: 0) + amount
    }

     fun removeCoins(amount: Int) {
        _coins.value = (_coins.value ?: 0) - amount
    }
}
