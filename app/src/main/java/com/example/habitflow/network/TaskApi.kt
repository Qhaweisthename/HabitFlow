package com.example.habitflow.network

import com.example.habitflow.model.Task
import retrofit2.Response
import retrofit2.http.*

interface TaskApi {
    @GET("api/tasks")
    suspend fun getTasks(): Response<List<Task>>

    @POST("api/tasks")
    suspend fun addTask(@Body task: Task): Response<Task>

    @PUT("api/tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body task: Task): Response<Task>

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<Unit>
}
