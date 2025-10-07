package com.example.habitflow.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "Task")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userEmail: String = "guest@habitflow.com", // ✅ default fallback

    val name: String,
    var isDone: Boolean = false,
    val date: String = getTodayDate(),
    val remoteId: String? = null
) {
    companion object {
        fun getTodayDate(): String {
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return format.format(Date())
        }
    }
}
