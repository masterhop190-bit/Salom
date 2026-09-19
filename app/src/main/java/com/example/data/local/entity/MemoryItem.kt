package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_items")
data class MemoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String, // "preference", "contact", "command", "custom"
    val updatedAt: Long = System.currentTimeMillis()
)
