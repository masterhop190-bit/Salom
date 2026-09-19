package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.example.BuildConfig
import java.nio.charset.StandardCharsets

class SecurePreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "nova_secure_prefs"
        private const val KEY_API_KEY_ENC = "key_api_key_enc"
        private const val KEY_SELECTED_MODEL = "key_selected_model"
        private const val KEY_PRIMARY_LANGUAGE = "key_primary_lang"
        private const val KEY_WAKE_WORD_ENABLED = "key_wake_word"
        private const val KEY_OVERLAY_ENABLED = "key_overlay_bubble"
        private const val KEY_CONTINUOUS_VOICE = "key_continuous_voice"
        private const val KEY_IS_ONBOARDED = "key_is_onboarded"
        private const val KEY_OFFLINE_PRIVACY = "key_offline_privacy"
        private const val KEY_TOTAL_TOKENS = "key_total_tokens"
        private const val KEY_TTS_SPEED = "key_tts_speed"
        private const val KEY_TTS_PITCH = "key_tts_pitch"
        private const val KEY_USER_NAME = "key_user_name"

        const val MODEL_FLASH = "gemini-3.5-flash"
        const val MODEL_PRO = "gemini-3.1-pro-preview"
        const val MODEL_FLASH_LITE = "gemini-3.1-flash-lite-preview"

        const val LANG_UZBEK = "uz"
        const val LANG_RUSSIAN = "ru"
        const val LANG_ENGLISH = "en"
    }

    var isOnboarded: Boolean
        get() = prefs.getBoolean(KEY_IS_ONBOARDED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ONBOARDED, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Commander") ?: "Commander"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    fun getApiKey(): String {
        val encoded = prefs.getString(KEY_API_KEY_ENC, null)
        if (!encoded.isNullOrBlank()) {
            return try {
                val decodedBytes = Base64.decode(encoded, Base64.DEFAULT)
                String(decodedBytes, StandardCharsets.UTF_8)
            } catch (e: Exception) {
                BuildConfig.GEMINI_API_KEY
            }
        }
        return BuildConfig.GEMINI_API_KEY
    }

    fun setApiKey(apiKey: String) {
        if (apiKey.isBlank()) {
            prefs.edit().remove(KEY_API_KEY_ENC).apply()
        } else {
            val encoded = Base64.encodeToString(apiKey.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)
            prefs.edit().putString(KEY_API_KEY_ENC, encoded).apply()
        }
    }

    fun hasCustomApiKey(): Boolean {
        return !prefs.getString(KEY_API_KEY_ENC, null).isNullOrBlank()
    }

    var selectedModel: String
        get() = prefs.getString(KEY_SELECTED_MODEL, MODEL_FLASH) ?: MODEL_FLASH
        set(value) = prefs.edit().putString(KEY_SELECTED_MODEL, value).apply()

    var primaryLanguage: String
        get() = prefs.getString(KEY_PRIMARY_LANGUAGE, LANG_UZBEK) ?: LANG_UZBEK
        set(value) = prefs.edit().putString(KEY_PRIMARY_LANGUAGE, value).apply()

    var wakeWordEnabled: Boolean
        get() = prefs.getBoolean(KEY_WAKE_WORD_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_WAKE_WORD_ENABLED, value).apply()

    var overlayBubbleEnabled: Boolean
        get() = prefs.getBoolean(KEY_OVERLAY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, value).apply()

    var continuousVoiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_CONTINUOUS_VOICE, false)
        set(value) = prefs.edit().putBoolean(KEY_CONTINUOUS_VOICE, value).apply()

    var offlinePrivacyMode: Boolean
        get() = prefs.getBoolean(KEY_OFFLINE_PRIVACY, false)
        set(value) = prefs.edit().putBoolean(KEY_OFFLINE_PRIVACY, value).apply()

    var totalTokensUsed: Int
        get() = prefs.getInt(KEY_TOTAL_TOKENS, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_TOKENS, value).apply()

    fun addTokens(count: Int) {
        totalTokensUsed += count
    }

    var ttsSpeed: Float
        get() = prefs.getFloat(KEY_TTS_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_TTS_SPEED, value).apply()

    var ttsPitch: Float
        get() = prefs.getFloat(KEY_TTS_PITCH, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_TTS_PITCH, value).apply()
}
