package com.example.habitflow.model

import java.text.SimpleDateFormat
import java.util.*

data class Task(
    val id: Int,
    val name: String,
    var isDone: Boolean = false,
    val date: String = getTodayDate() // default = today
) {
    companion object {
        fun getTodayDate(): String {
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return format.format(Date())
        }
    }
}
