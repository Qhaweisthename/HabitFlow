package com.example.habitflow.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["remoteId"], unique = true)]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String = "guest@habitflow.com", // fallback for offline/guest
    val name: String,
    var isDone: Boolean = false,
    val date: String = getTodayDate(),
    val remoteId: String? = null,
    val isSynced: Boolean = false // tracks offline sync state
) {
    companion object {
        fun getTodayDate(): String {
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return format.format(Date())
        }
    }
}
