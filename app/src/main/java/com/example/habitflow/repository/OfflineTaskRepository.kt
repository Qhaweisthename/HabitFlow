//package com.example.habitflow.repository
//
//import android.content.Context
//import com.example.habitflow.data.TaskDao
//import com.example.habitflow.data.model.Task
//import com.example.habitflow.network.ApiService
//import com.example.habitflow.ui.tasks.CreateTaskRequest
//import com.example.habitflow.util.NetworkUtils
//
//class OfflineTaskRepository(
//    private val api: ApiService,
//    private val dao: TaskDao,
//    private val context: Context
//) {
//
//    suspend fun addTask(task: Task) {
//        val localId = dao.insertTask(task)
//
//        // If offline: stop here
//        if (!NetworkUtils.isOnline(context)) return
//
//        try {
//            val res = api.addTask(CreateTaskRequest(task.name, task.isDone, task.date))
//            if (res.isSuccessful && res.body() != null) {
//                val remote = res.body()!!
//                dao.markAsSynced(localId.toInt(), remote._id!!)
//            }
//        } catch (_: Exception) { /* stays unsynced */ }
//    }
//
//    suspend fun syncPending() {
//        if (!NetworkUtils.isOnline(context)) return
//
//        val unsyncedTasks = dao.getUnsyncedTasks()
//
//        for (task in unsyncedTasks) {
//            try {
//                val res = api.addTask(CreateTaskRequest(task.name, task.isDone, task.date))
//                if (res.isSuccessful && res.body() != null) {
//                    val remote = res.body()!!
//                    dao.markAsSynced(task.id, remote._id!!)
//                }
//            } catch (_: Exception) {}
//        }
//    }
//}
