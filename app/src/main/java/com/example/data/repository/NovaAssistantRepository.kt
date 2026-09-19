package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.dao.AutomationDao
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.DocumentDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.entity.AutomationShortcut
import com.example.data.local.entity.ConversationMessage
import com.example.data.local.entity.GeneratedDocument
import com.example.data.local.entity.MemoryItem
import com.example.data.preferences.SecurePreferencesManager
import com.example.data.remote.GeminiApiService
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerateRequest
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.NovaActionPayload
import com.example.data.remote.NovaRetrofitClient
import com.example.engine.NovaAccessibilityController
import com.example.engine.NovaDocumentGenerator
import com.example.engine.NovaScreenAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class NovaAssistantRepository(
    private val conversationDao: ConversationDao,
    private val automationDao: AutomationDao,
    private val memoryDao: MemoryDao,
    private val documentDao: DocumentDao,
    val preferencesManager: SecurePreferencesManager,
    private val geminiService: GeminiApiService,
    val context: Context
) {

    val allMessages: Flow<List<ConversationMessage>> = conversationDao.getAllMessages()
    val allShortcuts: Flow<List<AutomationShortcut>> = automationDao.getAllShortcuts()
    val allMemory: Flow<List<MemoryItem>> = memoryDao.getAllMemory()
    val allDocuments: Flow<List<GeneratedDocument>> = documentDao.getAllDocuments()

    val accessibilityController = NovaAccessibilityController(context)
    val documentGenerator = NovaDocumentGenerator(context)
    val screenAnalyzer = NovaScreenAnalyzer(context)

    suspend fun saveMessage(message: ConversationMessage): Long {
        return conversationDao.insertMessage(message)
    }

    suspend fun clearHistory() {
        conversationDao.clearAll()
    }

    suspend fun saveShortcut(shortcut: AutomationShortcut): Long {
        return automationDao.insertShortcut(shortcut)
    }

    suspend fun deleteShortcut(id: Long) {
        automationDao.deleteShortcutById(id)
    }

    suspend fun saveMemory(key: String, value: String, category: String = "preference") {
        memoryDao.insertOrUpdate(MemoryItem(key = key, value = value, category = category))
    }

    suspend fun processUserPrompt(
        prompt: String,
        isVoiceInput: Boolean = false,
        screenBitmap: Bitmap? = null,
        onStatusUpdate: ((String) -> Unit)? = null
    ): ProcessResult = withContext(Dispatchers.IO) {
        // Save user message
        conversationDao.insertMessage(
            ConversationMessage(
                role = "user",
                content = prompt,
                isVoice = isVoiceInput
            )
        )

        val apiKey = preferencesManager.getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val errorMsg = "Iltimos, Gemini API kalitini sozlamalar bo'limida kiriting (Please provide your Gemini API Key in Settings)."
            conversationDao.insertMessage(
                ConversationMessage(
                    role = "assistant",
                    content = errorMsg
                )
            )
            return@withContext ProcessResult(
                spokenResponse = errorMsg,
                displayText = errorMsg
            )
        }

        onStatusUpdate?.invoke("Nova tahlil qilmoqda...")

        // Build compact system instruction with memory context & language preference
        val lang = preferencesManager.primaryLanguage
        val langInstruction = when (lang) {
            "uz" -> "Primary conversation language is Uzbek (O'zbekcha). Use natural, fluent Uzbek by default."
            "ru" -> "Primary conversation language is Russian (Русский). Use natural, fluent Russian by default."
            else -> "Primary conversation language is English. Use natural, fluent English."
        }

        val systemPrompt = """
            You are Nova, an autonomous Jarvis-like AI Assistant and Personal Companion on Android.
            $langInstruction
            
            TOKEN SAVINGS INSTRUCTION: Keep responses concise, direct, and structured.
            
            When user asks for an Android action or GUI automation (e.g. sending a message on Telegram/WhatsApp, opening an app, calling someone, tapping an on-screen element):
            Return a JSON object in this exact format:
            {
              "type": "action",
              "action": "send_telegram|open_app|call_phone|click|type_text|scroll_down|scroll_up|back|home",
              "targetApp": "org.telegram.messenger",
              "targetQuery": "contact name or button text",
              "phoneNumber": "+998901234567 or contact name",
              "textToType": "message content if applicable",
              "spokenResponse": "Short spoken response in user's language",
              "displayText": "Clear status confirmation"
            }
            
            When user asks to generate a document or code (e.g., "Make me a .docx file containing 1 car image", "Generate python script for ...", "Create a PDF report"):
            Return a JSON object:
            {
              "type": "generate_file",
              "fileType": "docx|pdf|txt|py|js|html",
              "fileTitle": "Short Title",
              "searchImageQuery": "car (if an image is requested, otherwise empty)",
              "fileContent": "Full formatted text or code content for the document",
              "spokenResponse": "Document created successfully notification",
              "displayText": "Summary of created file"
            }
            
            For all standard questions and chats:
            Return a JSON object:
            {
              "type": "response",
              "spokenResponse": "Concise spoken answer for TTS (1-2 sentences)",
              "displayText": "Complete, beautifully formatted markdown response"
            }
            
            ONLY return valid JSON. Do not wrap in markdown codeblocks if possible.
        """.trimIndent()

        val recentMessages = conversationDao.getRecentMessages(6).reversed()
        val contents = mutableListOf<GeminiContent>()

        for (msg in recentMessages) {
            val role = if (msg.role == "user") "user" else "model"
            contents.add(
                GeminiContent(
                    role = role,
                    parts = listOf(GeminiPart(text = msg.content))
                )
            )
        }

        // Multimodal part if screenBitmap is supplied
        if (screenBitmap != null) {
            val base64Screen = screenAnalyzer.compressBitmapToBase64(screenBitmap)
            contents.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(text = "Current Screen Context: $prompt"),
                        GeminiPart(
                            inlineData = com.example.data.remote.GeminiInlineData(
                                mimeType = "image/jpeg",
                                data = base64Screen
                            )
                        )
                    )
                )
            )
        } else if (contents.isEmpty() || contents.lastOrNull()?.role != "user") {
            contents.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        }

        val model = preferencesManager.selectedModel
        val request = GeminiGenerateRequest(
            contents = contents,
            generationConfig = GeminiGenerationConfig(
                temperature = 0.5f,
                maxOutputTokens = 1200,
                responseMimeType = "application/json"
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            )
        )

        try {
            val response = geminiService.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )

            val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "{}"
            val tokensUsed = response.usageMetadata?.totalTokenCount ?: 0
            preferencesManager.addTokens(tokensUsed)

            // Parse Action / Response
            val payload = parsePayload(rawJson)

            when (payload.type) {
                "action" -> {
                    onStatusUpdate?.invoke("Avtomatlashtirilgan harakat bajarilmoqda...")
                    accessibilityController.executeAction(payload) { status ->
                        onStatusUpdate?.invoke(status)
                    }
                    val displayText = payload.displayText ?: payload.spokenResponse ?: "Harakat bajarildi."
                    val spokenText = payload.spokenResponse ?: displayText

                    conversationDao.insertMessage(
                        ConversationMessage(
                            role = "assistant",
                            content = displayText,
                            actionJson = rawJson,
                            tokenCount = tokensUsed
                        )
                    )

                    return@withContext ProcessResult(
                        spokenResponse = spokenText,
                        displayText = displayText,
                        actionPayload = payload,
                        tokensUsed = tokensUsed
                    )
                }

                "generate_file" -> {
                    onStatusUpdate?.invoke("Fayl generatsiya qilinmoqda...")
                    val title = payload.fileTitle ?: "Nova_Document"
                    val content = payload.fileContent ?: "Nova Generated Content"
                    val ext = (payload.fileType ?: "txt").lowercase()

                    val generatedFile = when (ext) {
                        "docx" -> documentGenerator.generateDocx(title, content, payload.searchImageQuery)
                        "pdf" -> documentGenerator.generatePdf(title, content, payload.searchImageQuery)
                        else -> documentGenerator.generateCodeOrTextFile(title, content, ext)
                    }

                    val docEntity = GeneratedDocument(
                        title = title,
                        fileType = ext,
                        filePath = generatedFile.absolutePath,
                        fileSize = generatedFile.length(),
                        query = prompt
                    )
                    documentDao.insertDocument(docEntity)

                    val displayText = "${payload.displayText ?: "Fayl muvaffaqiyatli yaratildi!"}\n\n📁 **Saqlangan fayl:** `${generatedFile.name}`\n📄 **Hajmi:** ${generatedFile.length() / 1024} KB"
                    val spokenText = payload.spokenResponse ?: "$title nomli fayl yaratildi."

                    conversationDao.insertMessage(
                        ConversationMessage(
                            role = "assistant",
                            content = displayText,
                            actionJson = rawJson,
                            tokenCount = tokensUsed,
                            mediaUri = generatedFile.absolutePath
                        )
                    )

                    return@withContext ProcessResult(
                        spokenResponse = spokenText,
                        displayText = displayText,
                        generatedFile = generatedFile,
                        tokensUsed = tokensUsed
                    )
                }

                else -> {
                    val displayText = payload.displayText ?: payload.spokenResponse ?: rawJson
                    val spokenText = payload.spokenResponse ?: displayText

                    conversationDao.insertMessage(
                        ConversationMessage(
                            role = "assistant",
                            content = displayText,
                            actionJson = rawJson,
                            tokenCount = tokensUsed
                        )
                    )

                    return@withContext ProcessResult(
                        spokenResponse = spokenText,
                        displayText = displayText,
                        tokensUsed = tokensUsed
                    )
                }
            }
        } catch (e: Exception) {
            val errorMsg = "Xatolik yuz berdi: ${e.message ?: "Aloqa uzildi"}"
            conversationDao.insertMessage(
                ConversationMessage(
                    role = "assistant",
                    content = errorMsg
                )
            )
            return@withContext ProcessResult(
                spokenResponse = "Xatolik yuz berdi",
                displayText = errorMsg
            )
        }
    }

    private fun parsePayload(raw: String): NovaActionPayload {
        return try {
            val clean = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val adapter = NovaRetrofitClient.moshi.adapter(NovaActionPayload::class.java)
            adapter.fromJson(clean) ?: NovaActionPayload(displayText = clean, spokenResponse = clean)
        } catch (e: Exception) {
            NovaActionPayload(displayText = raw, spokenResponse = raw)
        }
    }
}

data class ProcessResult(
    val spokenResponse: String,
    val displayText: String,
    val actionPayload: NovaActionPayload? = null,
    val generatedFile: File? = null,
    val tokensUsed: Int = 0
)
