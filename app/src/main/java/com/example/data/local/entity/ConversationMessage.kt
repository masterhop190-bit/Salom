package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user", "assistant", "system", "action"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionJson: String? = null,
    val tokenCount: Int = 0,
    val isVoice: Boolean = false,
    val mediaUri: String? = null
)
