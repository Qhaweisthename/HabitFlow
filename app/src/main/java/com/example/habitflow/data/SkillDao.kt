package com.example.habitflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.habitflow.data.model.Skill

@Dao
interface SkillDao {

    @Query("SELECT COUNT(*) FROM skills")
    suspend fun getCount(): Int

    @Query("SELECT * FROM skills")
    suspend fun getAll(): List<Skill>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(skills: List<Skill>)

    // Skills available for a given player level
    @Query("SELECT * FROM skills WHERE minLevel <= :level ORDER BY minLevel, cost, name")
    suspend fun getUnlockedForLevel(level: Int): List<Skill>

    // Room needs a List, not a Set, for the IN clause
    @Query("SELECT * FROM skills WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<Skill>
}
