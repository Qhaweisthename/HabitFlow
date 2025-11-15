// com/example/habitflow/network/dto/ApiTask.kt
package com.example.habitflow.network.dto

data class ApiTask(
    val _id: String,            // Mongo id
    val name: String,
    val isDone: Boolean,
    val date: String
)

data class CreateTaskRequest(
    val name: String,
    val isDone: Boolean,
    val date: String
)

data class UpdateTaskRequest(
    val name: String,
    val isDone: Boolean,
    val date: String
)
