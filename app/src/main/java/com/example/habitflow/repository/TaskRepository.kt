package com.example.habitflow.repository

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.data.model.Task
import com.example.habitflow.network.ApiService
import com.example.habitflow.ui.tasks.CreateTaskRequest
import com.example.habitflow.ui.tasks.UpdateTaskRequest
import com.example.habitflow.util.NetworkUtils

class TaskRepository(
    context: Context,
    private val api: ApiService
) {

    private val taskDao = AppDatabase.getInstance(context).taskDao()
    private val appContext = context.applicationContext

    // ------------------------------
    // LOAD
    // ------------------------------

    suspend fun loadLocal(email: String): List<Task> {
        return taskDao.getTasksForUser(email)
    }

    suspend fun insertIfNotExists(task: Task) {
        val existing = when {
            task.remoteId != null -> taskDao.findByRemoteId(task.remoteId)
            else -> taskDao.findByNameAndDate(task.name, task.date)
        }

        if (existing != null) return
    }

    // ------------------------------
    // ADD
    // ------------------------------

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    suspend fun addTask(task: Task) {

        // Stop dupes BEFORE insert
        val exists = if (task.remoteId != null)
            taskDao.findByRemoteId(task.remoteId)
        else
            taskDao.findByNameAndDate(task.name, task.date)

        if (exists != null) return


        val localId = taskDao.insertTask(task).toInt()

        if (NetworkUtils.isOnline(appContext)) {
            try {
                val response = api.addTask(
                    CreateTaskRequest(
                        name = task.name,
                        isDone = task.isDone,
                        date = task.date
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    taskDao.markAsSynced(localId, response.body()!!._id!!)
                }
            } catch (_: Exception) {}
        }
    }

    // ------------------------------
    // UPDATE
    // ------------------------------

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)

        if (NetworkUtils.isOnline(appContext)) {
            try {
                task.remoteId?.let {
                    api.updateTask(it, UpdateTaskRequest(task.name, task.isDone, task.date))
                }
            } catch (_: Exception) {}
        }
    }

    // ------------------------------
    // DELETE
    // ------------------------------

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)

        if (NetworkUtils.isOnline(appContext)) {
            try {
                task.remoteId?.let { api.deleteTask(it) }
            } catch (_: Exception) {}
        }
    }

    // ------------------------------
    // CLEAR ALL
    // ------------------------------

    suspend fun clearAll(email: String) {
        taskDao.deleteAllForUser(email)
    }

    // ------------------------------
    // SYNC
    // ------------------------------

    suspend fun syncPending() {
        if (!NetworkUtils.isOnline(appContext)) return

        val unsynced = taskDao.getUnsyncedTasks()

        for (task in unsynced) {
            try {
                val response = api.addTask(
                    CreateTaskRequest(task.name, task.isDone, task.date)
                )

                if (response.isSuccessful && response.body() != null) {
                    taskDao.markAsSynced(task.id, response.body()!!._id!!)
                }
            } catch (_: Exception) {}
        }
    }
}
