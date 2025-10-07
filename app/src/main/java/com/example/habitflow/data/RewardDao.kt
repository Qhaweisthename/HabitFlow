package com.example.habitflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.habitflow.model.Reward

@Dao
interface RewardDao {
    @Query("SELECT * FROM Reward WHERE userEmail = :email")
    suspend fun getRewardsForUser(email: String): List<Reward>

    @Insert
    suspend fun insertReward(reward: Reward)
}
