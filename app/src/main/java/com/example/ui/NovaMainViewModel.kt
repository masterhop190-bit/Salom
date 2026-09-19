package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NovaApplication
import com.example.data.local.entity.AutomationShortcut
import com.example.data.local.entity.ConversationMessage
import com.example.data.local.entity.GeneratedDocument
import com.example.data.local.entity.MemoryItem
import com.example.data.preferences.SecurePreferencesManager
import com.example.data.repository.NovaAssistantRepository
import com.example.engine.NovaVoiceEngine
import com.example.service.NovaAccessibilityService
import com.example.service.NovaOverlayService
import com.example.service.NovaNotificationManager
import com.example.service.NovaWakeWordService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class NovaMainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NovaApplication
    private val repository: NovaAssistantRepository = app.repository
    val preferencesManager: SecurePreferencesManager = app.preferencesManager

    val messages: StateFlow<List<ConversationMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortcuts: StateFlow<List<AutomationShortcut>> = repository.allShortcuts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<GeneratedDocument>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memoryItems: StateFlow<List<MemoryItem>> = repository.allMemory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isAccessibilityActive: StateFlow<Boolean> = NovaAccessibilityService.isServiceActive
    val isWakeWordActive: StateFlow<Boolean> = NovaWakeWordService.isRunning
    val isOverlayActive: StateFlow<Boolean> = NovaOverlayService.isOverlayActive

    val voiceEngine = NovaVoiceEngine(application)
    val isListening: StateFlow<Boolean> = voiceEngine.isListening
    val isSpeaking: StateFlow<Boolean> = voiceEngine.isSpeaking
    val rmsAmplitude: StateFlow<Float> = voiceEngine.rmsAmplitude
    val recognizedSpeech: StateFlow<String> = voiceEngine.recognizedText

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _currentStatusText = MutableStateFlow("JARVIS CORE TAYYOR • \"Hi Nova\" yoki buyruq bering")
    val currentStatusText: StateFlow<String> = _currentStatusText.asStateFlow()

    private val _liveModeActive = MutableStateFlow(false)
    val liveModeActive: StateFlow<Boolean> = _liveModeActive.asStateFlow()

    private val _showOnboarding = MutableStateFlow(!preferencesManager.isOnboarded)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    private val _screenHierarchyPreview = MutableStateFlow("")
    val screenHierarchyPreview: StateFlow<String> = _screenHierarchyPreview.asStateFlow()

    init {
        voiceEngine.setLanguage(preferencesManager.primaryLanguage)

        voiceEngine.onSpeechResult = { text ->
            if (text.isNotBlank()) {
                handleUserPrompt(text, isVoice = true)
            }
        }

        voiceEngine.onListeningStateChanged = { listening ->
            NovaNotificationManager.showJarvisNotification(app, listening)
        }

        // Post persistent Jarvis voice notification on app launch
        NovaNotificationManager.showJarvisNotification(app, false)

        // Initialize default shortcuts if empty
        viewModelScope.launch {
            if (repository.allShortcuts.stateIn(this).value.isEmpty()) {
                repository.saveShortcut(
                    AutomationShortcut(
                        title = "Telefon Qilish: +998901234567",
                        triggerPhrase = "Call +998901234567",
                        targetAppPackage = "com.android.dialer",
                        actionScriptJson = """{"type":"action","action":"call_phone","phoneNumber":"+998901234567"}"""
                    )
                )
                repository.saveShortcut(
                    AutomationShortcut(
                        title = "Telegram Xabar: Onamga",
                        triggerPhrase = "Telegram’da Onamga 'Bordim' deb xabar yubor",
                        targetAppPackage = "org.telegram.messenger",
                        actionScriptJson = """{"type":"action","action":"send_telegram","targetQuery":"Onam","textToType":"Bordim"}"""
                    )
                )
                repository.saveShortcut(
                    AutomationShortcut(
                        title = "Avtomobil rasmi bilan DOCX",
                        triggerPhrase = "Make me a .docx file containing 1 car image",
                        targetAppPackage = "com.microsoft.office.word",
                        actionScriptJson = """{"type":"generate_file","fileType":"docx","fileTitle":"Avtomobil Hisoboti","searchImageQuery":"car","fileContent":"Bu hujjat Nova AI tomonidan yaratildi."}"""
                    )
                )
                repository.saveShortcut(
                    AutomationShortcut(
                        title = "Ekran Tahlili (Vision)",
                        triggerPhrase = "Ekranda nima bor?",
                        targetAppPackage = "system.screen",
                        actionScriptJson = """{"type":"action","action":"vision_query"}"""
                    )
                )
            }
        }
    }

    fun completeOnboarding(apiKey: String, selectedLanguage: String) {
        if (apiKey.isNotBlank()) {
            preferencesManager.setApiKey(apiKey)
        }
        preferencesManager.primaryLanguage = selectedLanguage
        preferencesManager.isOnboarded = true
        _showOnboarding.value = false
        voiceEngine.setLanguage(selectedLanguage)
    }

    fun toggleVoiceListening() {
        if (isListening.value) {
            voiceEngine.stopListening()
            _currentStatusText.value = "Kutish rejimida"
            NovaNotificationManager.showJarvisNotification(app, false)
        } else {
            voiceEngine.stopSpeaking()
            _currentStatusText.value = "Tinglanmoqda... Gapiring"
            voiceEngine.startListening(langCode = preferencesManager.primaryLanguage)
            NovaNotificationManager.showJarvisNotification(app, true)
        }
    }

    fun toggleLiveMode() {
        val next = !_liveModeActive.value
        _liveModeActive.value = next
        if (next) {
            _currentStatusText.value = "Live Interaktiv Rejim Faol"
            voiceEngine.startListening(langCode = preferencesManager.primaryLanguage)
            NovaNotificationManager.showJarvisNotification(app, true)
        } else {
            voiceEngine.stopListening()
            voiceEngine.stopSpeaking()
            _currentStatusText.value = "Live rejim to'xtatildi"
            NovaNotificationManager.showJarvisNotification(app, false)
        }
    }

    fun handleUserPrompt(prompt: String, isVoice: Boolean = false, screenBitmap: Bitmap? = null) {
        if (prompt.isBlank() || _isProcessing.value) return

        _isProcessing.value = true
        _currentStatusText.value = "Nova tahlil qilmoqda..."

        viewModelScope.launch {
            try {
                // Direct Phone Call check for ultra-fast execution
                val lowerPrompt = prompt.lowercase().trim()
                if (lowerPrompt.startsWith("call ") || lowerPrompt.startsWith("qo'ng'iroq qil ") || 
                    lowerPrompt.startsWith("telefon qil ") || lowerPrompt.startsWith("позвони ")) {
                    val phonePart = prompt.substringAfter(" ").trim()
                    if (phonePart.isNotEmpty()) {
                        makePhoneCall(phonePart)
                    }
                }

                val result = repository.processUserPrompt(
                    prompt = prompt,
                    isVoiceInput = isVoice,
                    screenBitmap = screenBitmap,
                    onStatusUpdate = { status ->
                        _currentStatusText.value = status
                    }
                )

                _currentStatusText.value = "Tayyor"

                // TTS playback: Speak back to talk with the user using AI
                voiceEngine.speak(
                    text = result.spokenResponse,
                    speed = preferencesManager.ttsSpeed,
                    pitch = preferencesManager.ttsPitch
                ) {
                    if (_liveModeActive.value) {
                        // In live continuous mode, automatically listen again
                        voiceEngine.startListening(langCode = preferencesManager.primaryLanguage)
                    }
                }
            } catch (e: Exception) {
                _currentStatusText.value = "Xatolik: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun makePhoneCall(numberOrQuery: String) {
        _currentStatusText.value = "Qo'ng'iroq qilinmoqda: $numberOrQuery"
        repository.accessibilityController.makePhoneCall(numberOrQuery)
    }

    fun triggerScreenVision() {
        _currentStatusText.value = "Ekran skanerlanmoqda..."
        val mockScreen = repository.screenAnalyzer.createMockScreenSnapshot()
        handleUserPrompt("Ekranda nima bor? Matn va elementlarni tahlil qil.", isVoice = true, screenBitmap = mockScreen)
    }

    fun triggerTelegramQuickAction() {
        handleUserPrompt("Nova, Telegram’da Onamga 'Bordim' deb xabar yubor", isVoice = true)
    }

    fun triggerDocxCarGeneration() {
        handleUserPrompt("Make me a .docx file containing 1 car image and brief description", isVoice = true)
    }

    fun refreshScreenHierarchy() {
        _screenHierarchyPreview.value = repository.accessibilityController.getScreenHierarchy()
    }

    fun openGeneratedFile(context: Context, doc: GeneratedDocument) {
        try {
            val file = File(doc.filePath)
            if (!file.exists()) return

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val mimeType = when (doc.fileType.lowercase()) {
                "pdf" -> "application/pdf"
                "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                "txt" -> "text/plain"
                "html" -> "text/html"
                "py", "js" -> "text/plain"
                else -> "*/*"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareGeneratedFile(context: Context, doc: GeneratedDocument) {
        try {
            val file = File(doc.filePath)
            if (!file.exists()) return

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Faylni ulashish"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleWakeWordService(context: Context, enable: Boolean) {
        preferencesManager.wakeWordEnabled = enable
        if (enable) {
            NovaWakeWordService.start(context)
        } else {
            NovaWakeWordService.stop(context)
        }
    }

    fun toggleOverlayService(context: Context, enable: Boolean) {
        preferencesManager.overlayBubbleEnabled = enable
        if (enable) {
            if (Settings.canDrawOverlays(context)) {
                NovaOverlayService.start(context)
            }
        } else {
            NovaOverlayService.stop(context)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.release()
    }
}
