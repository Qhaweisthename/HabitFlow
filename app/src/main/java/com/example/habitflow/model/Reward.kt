package com.example.habitflow.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Reward")
data class Reward(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String,
    val name: String,
    val cost: Int,
    var claimed: Boolean = false
)
