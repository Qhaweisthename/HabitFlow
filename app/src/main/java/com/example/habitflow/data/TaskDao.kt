package com.example.habitflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.habitflow.data.model.Task

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE userEmail = :email")
    suspend fun getTasksForUser(email: String): List<Task>

    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<Task>

    @Query("UPDATE tasks SET isSynced = 1, remoteId = :remoteId WHERE id = :localId")
    suspend fun markAsSynced(localId: Int, remoteId: String)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE userEmail = :email")
    suspend fun deleteAllForUser(email: String)

    @Query("SELECT * FROM tasks WHERE name = :name AND date = :date LIMIT 1")
    suspend fun findByNameAndDate(name: String, date: String): Task?

    @Query("SELECT * FROM tasks WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: String): Task?
}

