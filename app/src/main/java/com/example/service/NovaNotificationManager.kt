package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NovaNotificationManager {

    const val CHANNEL_ID = "nova_jarvis_control_channel"
    const val NOTIFICATION_ID = 9001

    const val ACTION_START_LISTENING = "com.example.nova.ACTION_START_LISTENING"
    const val ACTION_STOP_LISTENING = "com.example.nova.ACTION_STOP_LISTENING"
    const val ACTION_TRIGGER_VOICE = "com.example.nova.ACTION_TRIGGER_VOICE"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nova Jarvis Voice Controller",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing Jarvis AI voice controls and status"
                setShowBadge(false)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showJarvisNotification(context: Context, isListening: Boolean) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Intent for Start/Stop Voice
        val toggleVoiceIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TOGGLE_VOICE_IMMEDIATELY", true)
        }
        val toggleVoicePendingIntent = PendingIntent.getActivity(
            context,
            1,
            toggleVoiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusTitle = if (isListening) "🎙️ NOVA JARVIS: TINGLANMOQDA..." else "🤖 NOVA JARVIS AI ONLINE"
        val statusText = if (isListening) "Sizni eshitmoqda... Gapiring" else "\"Hi Nova\" deb chaqiring yoki [Boshlash] tugmasini bosing"
        val actionButtonLabel = if (isListening) "⏹️ To'xtatish" else "🎙️ Tinglashni Boshlash"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(statusTitle)
            .setContentText(statusText)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_btn_speak_now,
                actionButtonLabel,
                toggleVoicePendingIntent
            )
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.cancel(NOTIFICATION_ID)
    }
}
