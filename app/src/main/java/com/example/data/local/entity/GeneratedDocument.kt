package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_documents")
data class GeneratedDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val fileType: String, // "docx", "pdf", "txt", "py", "js", "html"
    val filePath: String,
    val fileSize: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val query: String = ""
)
