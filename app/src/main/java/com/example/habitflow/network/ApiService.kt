package com.example.habitflow.network

import com.example.habitflow.ApiTask
import com.example.habitflow.CreateTaskRequest
import com.example.habitflow.UpdateTaskRequest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    /** --- GET all tasks --- **/
    @GET("tasks")
    suspend fun getTasks(): Response<List<ApiTask>>

    /** --- POST new task --- **/
    @POST("tasks")
    suspend fun addTask(
        @Body request: CreateTaskRequest
    ): Response<ApiTask>

    /** --- PUT update task --- **/
    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Body request: UpdateTaskRequest
    ): Response<ApiTask>

    /** --- DELETE a task --- **/
    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Path("id") id: String
    ): Response<Unit>
}
