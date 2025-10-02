package com.example.habitflow.model

data class Reward(
    val id: Int,
    val name: String,
    val cost: Int,
    var claimed: Boolean = false
)
