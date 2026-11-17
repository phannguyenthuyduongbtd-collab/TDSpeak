package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _completedUtterance = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val completedUtterance: SharedFlow<String> = _completedUtterance.asSharedFlow()

    private var pendingUtterance: Triple<String, Float, String>? = null

    private var initFailed = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts ?: return
            val result = engine.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.ENGLISH)
            }
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    utteranceId?.let { _completedUtterance.tryEmit(it) }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    utteranceId?.let { _completedUtterance.tryEmit(it) }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    utteranceId?.let { _completedUtterance.tryEmit(it) }
                }
            })
            _isReady.value = true
            pendingUtterance?.let { (text, rate, id) ->
                pendingUtterance = null
                speakInternal(text, rate, id)
            }
        } else {
            initFailed = true
            _isReady.value = false
            _isSpeaking.value = false
            pendingUtterance?.let { (_, _, id) ->
                pendingUtterance = null
                _completedUtterance.tryEmit(id)
            }
        }
    }

    fun speak(text: String, rate: Float = 1.0f) {
        speakWithId(text, rate)
    }

    fun speakWithId(text: String, rate: Float = 1.0f): String {
        val clean = text.trim()
        val id = UUID.randomUUID().toString()
        if (clean.isEmpty() || initFailed || tts == null) {
            _isSpeaking.value = false
            _completedUtterance.tryEmit(id)
            return id
        }
        if (!_isReady.value) {
            pendingUtterance = Triple(clean, rate, id)
            return id
        }
        speakInternal(clean, rate, id)
        return id
    }

    private fun speakInternal(clean: String, rate: Float, utteranceId: String) {
        val engine = tts
        if (engine == null) {
            _isSpeaking.value = false
            _completedUtterance.tryEmit(utteranceId)
            return
        }
        engine.setSpeechRate(rate.coerceIn(0.4f, 1.5f))
        val res = engine.speak(clean, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (res != TextToSpeech.SUCCESS) {
            _isSpeaking.value = false
            _completedUtterance.tryEmit(utteranceId)
        }
    }

    fun stop() {
        pendingUtterance = null
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        _isSpeaking.value = false
    }

    fun release() {
        pendingUtterance = null
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
        _isReady.value = false
        _isSpeaking.value = false
    }
}
