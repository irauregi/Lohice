package com.example.tasktracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val orderIndex: Int = 0,
    val isArchived: Boolean = false
)
