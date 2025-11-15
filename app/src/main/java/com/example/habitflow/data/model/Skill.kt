package com.example.habitflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class Skill(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val cost: Int,
    val minLevel: Int // minimum player level required to unlock/purchase
)
