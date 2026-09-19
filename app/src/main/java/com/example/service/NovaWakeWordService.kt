package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.NovaApplication
import com.example.R
import com.example.engine.NovaVoiceEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NovaWakeWordService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var voiceEngine: NovaVoiceEngine? = null

    override fun onCreate() {
        super.onCreate()
        _isRunning.value = true
        voiceEngine = NovaVoiceEngine(this).apply {
            onWakeWordDetected = {
                // Wake word detected! Launch MainActivity or Overlay
                val intent = Intent(this@NovaWakeWordService, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("TRIGGER_VOICE_IMMEDIATELY", true)
                }
                startActivity(intent)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        startListeningLoop()
        return START_STICKY
    }

    private fun startListeningLoop() {
        serviceScope.launch {
            while (_isRunning.value) {
                try {
                    val app = NovaApplication.instance
                    val lang = app.preferencesManager.primaryLanguage
                    voiceEngine?.startListening(langCode = lang, isWakeWordMode = true)
                    delay(8000)
                } catch (e: Exception) {
                    delay(3000)
                }
            }
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NovaApplication.CHANNEL_ID_NOVA)
            .setContentTitle("Nova AI Assistant")
            .setContentText("Listening for 'Ok Nova' wake word...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        _isRunning.value = false
        voiceEngine?.release()
        voiceEngine = null
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 2001
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, NovaWakeWordService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, NovaWakeWordService::class.java))
        }
    }
}
