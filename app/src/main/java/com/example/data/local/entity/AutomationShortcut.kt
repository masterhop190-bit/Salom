package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_shortcuts")
data class AutomationShortcut(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val triggerPhrase: String,
    val targetAppPackage: String,
    val actionScriptJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val usageCount: Int = 0
)
