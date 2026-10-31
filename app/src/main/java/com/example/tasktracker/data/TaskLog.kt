package com.example.tasktracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_logs")
data class TaskLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val taskId: Long,
    val result: String, // "DONE" ("Lo hice") o "NOT_DONE" ("No lo hice")
    val timestamp: Long = System.currentTimeMillis()
)
