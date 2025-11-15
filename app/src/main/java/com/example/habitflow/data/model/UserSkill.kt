package com.example.habitflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "user_skills",
    primaryKeys = ["userEmail", "skillId"],
    indices = [Index("skillId")]
)
data class UserSkill(
    val userEmail: String,
    val skillId: Int,
    val acquiredAt: Long = System.currentTimeMillis()
)
