package com.example.habitflow.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.habitflow.data.model.Reward
import com.example.habitflow.data.model.Task
import com.example.habitflow.data.model.Skill
import com.example.habitflow.data.model.UserSkill
import com.example.habitflow.data.dao.SkillDao
import com.example.habitflow.data.dao.UserSkillDao


@Database(
    entities = [User::class, Task::class, Reward::class, Skill::class, UserSkill::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun rewardDao(): RewardDao
    abstract fun skillDao(): SkillDao
    abstract fun userSkillDao(): UserSkillDao
}
