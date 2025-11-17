package com.example.habitflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.habitflow.data.dao.SkillDao
import com.example.habitflow.data.dao.UserSkillDao
import com.example.habitflow.data.model.Reward
import com.example.habitflow.data.model.Skill
import com.example.habitflow.data.model.Task
import com.example.habitflow.data.model.User
import com.example.habitflow.data.model.UserSkill

@Database(
    entities = [User::class, Task::class, Reward::class, Skill::class, UserSkill::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun rewardDao(): RewardDao
    abstract fun skillDao(): SkillDao
    abstract fun userSkillDao(): UserSkillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habitflow_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
