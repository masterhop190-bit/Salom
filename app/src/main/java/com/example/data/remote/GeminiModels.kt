package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiGenerateRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "role") val role: String? = null,
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null,
    @Json(name = "responseMimeType") val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @Json(name = "usageMetadata") val usageMetadata: GeminiUsageMetadata? = null,
    @Json(name = "error") val error: GeminiError? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiUsageMetadata(
    @Json(name = "promptTokenCount") val promptTokenCount: Int? = 0,
    @Json(name = "candidatesTokenCount") val candidatesTokenCount: Int? = 0,
    @Json(name = "totalTokenCount") val totalTokenCount: Int? = 0
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    @Json(name = "code") val code: Int? = 0,
    @Json(name = "message") val message: String? = null,
    @Json(name = "status") val status: String? = null
)

// Autonomous Action Schema parsed from structured responses
@JsonClass(generateAdapter = true)
data class NovaActionPayload(
    @Json(name = "type") val type: String? = "response", // "response", "action", "generate_file", "search"
    @Json(name = "action") val action: String? = null, // "open_app", "click", "type_text", "scroll", "send_telegram", "back", "home", "vision_analysis"
    @Json(name = "targetApp") val targetApp: String? = null, // e.g. "org.telegram.messenger", "com.whatsapp"
    @Json(name = "targetQuery") val targetQuery: String? = null, // target contact, button label, or search text
    @Json(name = "textToType") val textToType: String? = null,
    @Json(name = "x") val x: Float? = null,
    @Json(name = "y") val y: Float? = null,
    @Json(name = "fileType") val fileType: String? = null, // "docx", "pdf", "txt", "py", "js", "html"
    @Json(name = "fileTitle") val fileTitle: String? = null,
    @Json(name = "fileContent") val fileContent: String? = null,
    @Json(name = "searchImageQuery") val searchImageQuery: String? = null,
    @Json(name = "phoneNumber") val phoneNumber: String? = null,
    @Json(name = "spokenResponse") val spokenResponse: String? = null,
    @Json(name = "displayText") val displayText: String? = null
)
