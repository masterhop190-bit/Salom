package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.GeneratedDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM generated_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<GeneratedDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: GeneratedDocument): Long

    @Query("DELETE FROM generated_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)
}
