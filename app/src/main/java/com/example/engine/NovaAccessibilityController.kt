package com.example.engine

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.remote.NovaActionPayload
import com.example.service.NovaAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class NovaAccessibilityController(private val context: Context) {

    fun isAccessibilityEnabled(): Boolean {
        return NovaAccessibilityService.instance != null
    }

    suspend fun executeAction(
        payload: NovaActionPayload,
        onStatus: (String) -> Unit
    ): Boolean = withContext(Dispatchers.Main) {
        val service = NovaAccessibilityService.instance

        when (payload.action) {
            "open_app", "launch_app" -> {
                val target = payload.targetApp ?: payload.targetQuery ?: ""
                onStatus("Target dastur ishga tushirilmoqda: $target")
                launchApp(target)
            }

            "call_phone", "dial_phone", "call" -> {
                val targetNumber = payload.phoneNumber ?: payload.targetQuery ?: ""
                onStatus("Qo'ng'iroq amalga oshirilmoqda: $targetNumber")
                makePhoneCall(targetNumber)
            }

            "send_telegram" -> {
                val contact = payload.targetQuery ?: "Onam"
                val text = payload.textToType ?: "Salom"
                onStatus("Telegram ochilmoqda...")

                // 1. Launch Telegram
                val opened = launchApp("org.telegram.messenger")
                if (!opened) {
                    launchApp("telegram")
                }
                delay(1200)

                if (service != null) {
                    onStatus("Kontakt qidirilmoqda: $contact")
                    // Search for contact
                    service.findAndClick(contact)
                    delay(800)

                    // Type message
                    onStatus("Xabar yozilmoqda: \"$text\"")
                    service.findAndSetText("Message", text)
                    delay(500)

                    // Click Send
                    onStatus("Yuborish tugmasi bosilmoqda")
                    val sent = service.findAndClick("Send") || service.findAndClick("Yuborish") || service.findAndClick("Отправить")
                    delay(300)
                    return@withContext sent
                }
                true
            }

            "click" -> {
                if (service != null) {
                    if (payload.targetQuery != null) {
                        onStatus("Element bosilmoqda: ${payload.targetQuery}")
                        service.findAndClick(payload.targetQuery)
                    } else if (payload.x != null && payload.y != null) {
                        onStatus("Koordinatalar bo'yicha bosilmoqda (${payload.x}, ${payload.y})")
                        service.clickCoordinates(payload.x, payload.y)
                        true
                    } else {
                        false
                    }
                } else {
                    onStatus("Accessibility xizmati yoqilmagan")
                    false
                }
            }

            "type_text" -> {
                if (service != null && payload.textToType != null) {
                    onStatus("Matn kiritilmoqda: ${payload.textToType}")
                    service.findAndSetText(payload.targetQuery ?: "", payload.textToType)
                } else {
                    false
                }
            }

            "scroll_down" -> {
                service?.performScroll(forward = true) ?: false
            }

            "scroll_up" -> {
                service?.performScroll(forward = false) ?: false
            }

            "back" -> {
                service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) ?: false
            }

            "home" -> {
                service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) ?: false
            }

            else -> true
        }
    }

    fun launchApp(packageNameOrQuery: String): Boolean {
        val pm = context.packageManager
        val query = packageNameOrQuery.lowercase().trim()

        val knownPackages = mapOf(
            "telegram" to "org.telegram.messenger",
            "whatsapp" to "com.whatsapp",
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "camera" to "com.google.android.GoogleCamera",
            "maps" to "com.google.android.apps.maps",
            "settings" to "com.android.settings",
            "gallery" to "com.google.android.apps.photos",
            "gmail" to "com.google.android.gm"
        )

        val targetPkg = knownPackages[query] ?: if (packageNameOrQuery.contains(".")) packageNameOrQuery else null

        if (targetPkg != null) {
            val intent = pm.getLaunchIntentForPackage(targetPkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            }
        }

        // Search installed apps matching name
        val intentList = pm.getInstalledApplications(0)
        for (app in intentList) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label.contains(query)) {
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                }
            }
        }

        // Web search fallback if app not installed
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$packageNameOrQuery")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun makePhoneCall(numberOrQuery: String): Boolean {
        return try {
            val cleanNumber = numberOrQuery.replace(Regex("[^0-9+]"), "")
            val dialNumber = if (cleanNumber.isNotEmpty()) cleanNumber else numberOrQuery
            
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$dialNumber")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } else {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$dialNumber")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                true
            }
        } catch (e: Exception) {
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$numberOrQuery")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                true
            } catch (err: Exception) {
                false
            }
        }
    }

    fun getScreenHierarchy(): String {
        return NovaAccessibilityService.instance?.getScreenNodeHierarchy()
            ?: "Accessibility service is inactive. Please enable Nova AI in Accessibility settings."
    }
}
