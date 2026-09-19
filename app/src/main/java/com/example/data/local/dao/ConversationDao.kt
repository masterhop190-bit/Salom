package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ConversationMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ConversationMessage>>

    @Query("SELECT * FROM conversations ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<ConversationMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationMessage): Long

    @Query("DELETE FROM conversations")
    suspend fun clearAll()

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteMessageById(id: Long)
}
