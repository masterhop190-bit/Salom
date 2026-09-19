package com.example.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class NovaVoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _rmsAmplitude = MutableStateFlow(0f)
    val rmsAmplitude: StateFlow<Float> = _rmsAmplitude.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    var onSpeechResult: ((String) -> Unit)? = null
    var onWakeWordDetected: (() -> Unit)? = null
    var onListeningStateChanged: ((Boolean) -> Unit)? = null

    init {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            setLanguage("uz")
        }
    }

    fun setLanguage(langCode: String) {
        val locale = when (langCode) {
            "uz" -> Locale("uz", "UZ")
            "ru" -> Locale("ru", "RU")
            else -> Locale.US
        }
        textToSpeech?.let {
            val result = it.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                it.language = Locale.US
            }
        }
    }

    fun startListening(langCode: String = "uz", isWakeWordMode: Boolean = false) {
        stopListening()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    onListeningStateChanged?.invoke(true)
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    _rmsAmplitude.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _rmsAmplitude.value = 0f
                    onListeningStateChanged?.invoke(false)
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _rmsAmplitude.value = 0f
                    onListeningStateChanged?.invoke(false)
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _rmsAmplitude.value = 0f
                    onListeningStateChanged?.invoke(false)

                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull() ?: return
                    _recognizedText.value = spokenText

                    if (isWakeWordMode) {
                        val lower = spokenText.lowercase()
                        if (lower.contains("hi nova") || lower.contains("hey nova") || lower.contains("ok nova") || 
                            lower.contains("nova") || lower.contains("salom nova") || lower.contains("нова") || 
                            lower.contains("привет нова") || lower.contains("jarvis") || lower.contains("hi jarvis")) {
                            onWakeWordDetected?.invoke()
                        }
                    } else {
                        onSpeechResult?.invoke(spokenText)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotEmpty()) {
                        _recognizedText.value = text
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val localeTag = when (langCode) {
            "uz" -> "uz-UZ"
            "ru" -> "ru-RU"
            else -> "en-US"
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // ignore
        }
        speechRecognizer = null
        _isListening.value = false
        _rmsAmplitude.value = 0f
        onListeningStateChanged?.invoke(false)
    }

    fun speak(text: String, speed: Float = 1.0f, pitch: Float = 1.0f, onDone: (() -> Unit)? = null) {
        if (!isTtsReady || text.isBlank()) return

        textToSpeech?.setSpeechRate(speed)
        textToSpeech?.setPitch(pitch)

        val utteranceId = "nova_utt_${System.currentTimeMillis()}"

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                onDone?.invoke()
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            // ignore
        }
        _isSpeaking.value = false
    }

    fun release() {
        stopListening()
        stopSpeaking()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
