package com.example.habitflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.habitflow.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM Task WHERE userEmail = :email")
    suspend fun getTasksForUser(email: String): List<Task>

    @Insert
    suspend fun insertTask(task: Task)
}
