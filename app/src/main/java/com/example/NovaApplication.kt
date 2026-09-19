package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.local.NovaDatabase
import com.example.data.preferences.SecurePreferencesManager
import com.example.data.remote.NovaRetrofitClient
import com.example.data.repository.NovaAssistantRepository

class NovaApplication : Application() {

    lateinit var database: NovaDatabase
        private set

    lateinit var preferencesManager: SecurePreferencesManager
        private set

    lateinit var repository: NovaAssistantRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = NovaDatabase.getDatabase(this)
        preferencesManager = SecurePreferencesManager(this)
        repository = NovaAssistantRepository(
            conversationDao = database.conversationDao(),
            automationDao = database.automationDao(),
            memoryDao = database.memoryDao(),
            documentDao = database.documentDao(),
            preferencesManager = preferencesManager,
            geminiService = NovaRetrofitClient.service,
            context = this
        )

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_NOVA,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_NOVA = "nova_background_channel"
        lateinit var instance: NovaApplication
            private set
    }
}
