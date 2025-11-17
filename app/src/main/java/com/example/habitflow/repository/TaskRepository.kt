package com.example.habitflow.repository

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.data.model.Task
import com.example.habitflow.network.ApiService
import com.example.habitflow.ui.tasks.CreateTaskRequest
import com.example.habitflow.util.NetworkUtils

class TaskRepository(
    context: Context,
    private val api: ApiService
) {

    private val taskDao = AppDatabase.getInstance(context).taskDao()
    private val appContext = context.applicationContext


    suspend fun deleteAllLocal(email: String) {
        taskDao.deleteAllForUser(email)
    }
    suspend fun deleteLocal(task: Task) {
        taskDao.deleteTask(task)
    }


    suspend fun insertIfNotExists(task: Task) {
        val existing =
            if (task.remoteId != null)
                taskDao.findByRemoteId(task.remoteId)
            else
                taskDao.findByNameAndDate(task.name, task.date)

        if (existing == null) {
            taskDao.insertTask(task)
        }
    }







    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    suspend fun addTask(task: Task) {

        // skip duplicates
        val existing = task.remoteId?.let { taskDao.findByRemoteId(it) }
        if (existing != null) return

        // insert
        val localId = taskDao.insertTask(task).toInt()

        // upload if online
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
                    val remote = response.body()!!
                    taskDao.markAsSynced(localId, remote._id!!)
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun syncPending() {
        if (!NetworkUtils.isOnline(appContext)) return

        val unsynced = taskDao.getUnsyncedTasks()

        for (task in unsynced) {
            try {
                val response = api.addTask(
                    CreateTaskRequest(
                        name = task.name,
                        isDone = task.isDone,
                        date = task.date
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val remote = response.body()!!
                    taskDao.markAsSynced(task.id, remote._id!!)
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun loadLocal(email: String): List<Task> {
        return taskDao.getTasksForUser(email)
    }
}

