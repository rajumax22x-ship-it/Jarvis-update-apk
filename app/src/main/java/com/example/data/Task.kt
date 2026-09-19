package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val isPriority: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
