package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MemoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_items ORDER BY updatedAt DESC")
    fun getAllMemory(): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memory_items WHERE `key` = :key LIMIT 1")
    suspend fun getByKey(key: String): MemoryItem?

    @Query("SELECT * FROM memory_items WHERE category = :category ORDER BY updatedAt DESC")
    suspend fun getByCategory(category: String): List<MemoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: MemoryItem): Long

    @Query("DELETE FROM memory_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM memory_items")
    suspend fun clearMemory()
}
