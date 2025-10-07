package com.example.habitflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.habitflow.data.model.UserSkill

@Dao
interface UserSkillDao {

    @Query("SELECT * FROM user_skills WHERE userEmail = :email")
    suspend fun getForUser(email: String): List<UserSkill>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userSkill: UserSkill)
}
