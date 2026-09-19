package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Base64
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerateRequest
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiInlineData
import com.example.data.remote.GeminiPart
import com.example.data.remote.NovaRetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class NovaScreenAnalyzer(private val context: Context) {

    fun compressBitmapToBase64(bitmap: Bitmap, quality: Int = 70): String {
        // Scale down if large to save token economy
        val maxDim = 1024
        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun analyzeScreen(
        bitmap: Bitmap,
        userPrompt: String,
        apiKey: String,
        model: String = "gemini-3.5-flash",
        systemInstruction: String = "Analyze the provided Android screen image concisely. Extract visible text, identify key interactive buttons, and provide direct answers in the user's language."
    ): String = withContext(Dispatchers.IO) {
        try {
            val base64Image = compressBitmapToBase64(bitmap)
            val request = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(
                            GeminiPart(text = userPrompt),
                            GeminiPart(
                                inlineData = GeminiInlineData(
                                    mimeType = "image/jpeg",
                                    data = base64Image
                                )
                            )
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.4f,
                    maxOutputTokens = 800
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemInstruction))
                )
            )

            val response = NovaRetrofitClient.service.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )

            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Screen analysis completed with no textual output."
        } catch (e: Exception) {
            "Vision analysis error: ${e.message ?: "Connection failed"}"
        }
    }

    fun createMockScreenSnapshot(): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#0A0E1A") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Header bar
        val barPaint = Paint().apply { color = Color.parseColor("#121B30") }
        canvas.drawRect(0f, 0f, width.toFloat(), 180f, barPaint)

        val textPaint = Paint().apply {
            color = Color.parseColor("#00F0FF")
            textSize = 52f
            isAntiAlias = true
        }
        canvas.drawText("Nova Autonomous Screen Inspector", 60f, 120f, textPaint)

        // Mock cards
        val cardPaint = Paint().apply { color = Color.parseColor("#1A2744") }
        val bodyTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            isAntiAlias = true
        }

        canvas.drawRoundRect(60f, 240f, width - 60f, 540f, 32f, 32f, cardPaint)
        canvas.drawText("Active App: Telegram Messenger", 100f, 340f, bodyTextPaint)
        canvas.drawText("Chat: Onam (Active)", 100f, 420f, bodyTextPaint)
        canvas.drawText("Status: [Send Message Button Visible]", 100f, 500f, textPaint)

        return bitmap
    }
}
