package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AutomationShortcut
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationDao {
    @Query("SELECT * FROM automation_shortcuts ORDER BY usageCount DESC, createdAt DESC")
    fun getAllShortcuts(): Flow<List<AutomationShortcut>>

    @Query("SELECT * FROM automation_shortcuts WHERE triggerPhrase LIKE '%' || :phrase || '%' LIMIT 1")
    suspend fun findByPhrase(phrase: String): AutomationShortcut?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: AutomationShortcut): Long

    @Update
    suspend fun updateShortcut(shortcut: AutomationShortcut)

    @Query("UPDATE automation_shortcuts SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: Long)

    @Query("DELETE FROM automation_shortcuts WHERE id = :id")
    suspend fun deleteShortcutById(id: Long)
}
