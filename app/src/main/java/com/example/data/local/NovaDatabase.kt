package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AutomationDao
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.DocumentDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.entity.AutomationShortcut
import com.example.data.local.entity.ConversationMessage
import com.example.data.local.entity.GeneratedDocument
import com.example.data.local.entity.MemoryItem

@Database(
    entities = [
        ConversationMessage::class,
        AutomationShortcut::class,
        MemoryItem::class,
        GeneratedDocument::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun automationDao(): AutomationDao
    abstract fun memoryDao(): MemoryDao
    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getDatabase(context: Context): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "nova_assistant_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
