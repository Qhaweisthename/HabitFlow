package com.example.habitflow.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.habitflow.model.Reward
import com.example.habitflow.model.Task

@Database(
    entities = [User::class, Task::class, Reward::class],
    version = 2, // ⬅️ increase this number (was 1 before)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun rewardDao(): RewardDao
}
