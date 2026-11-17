package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface SpeechRecognitionState {
    data object Idle : SpeechRecognitionState
    data class Listening(val rmsDb: Float = 0f, val partialText: String = "") : SpeechRecognitionState
    data object Processing : SpeechRecognitionState
    data class Result(val transcript: String) : SpeechRecognitionState
    data class Error(val message: String) : SpeechRecognitionState
}

class SpeechRecognizerManager(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null

    private val _state = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    val state: StateFlow<SpeechRecognitionState> = _state.asStateFlow()

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        if (!isAvailable) {
            _state.value = SpeechRecognitionState.Error(
                "Thiết bị chưa có dịch vụ nhận diện giọng nói (Google Speech Services). Bạn có thể nhập hoặc kiểm tra cài đặt Google trên máy."
            )
            return
        }

        stopAndDestroy()

        try {
            val sr = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            recognizer = sr

            sr.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = SpeechRecognitionState.Listening(0f, "")
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {
                    val current = _state.value
                    val partial = if (current is SpeechRecognitionState.Listening) current.partialText else ""
                    _state.value = SpeechRecognitionState.Listening(rmsdB.coerceIn(0f, 12f), partial)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _state.value = SpeechRecognitionState.Processing
                }

                override fun onError(error: Int) {
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Lỗi thu âm thanh từ micro."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Chưa được cấp quyền Micro."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                            "Không có kết nối mạng cho nhận diện giọng nói."
                        SpeechRecognizer.ERROR_NO_MATCH ->
                            "Không nghe rõ câu tiếng Anh. Chạm micro để nói lại nhé!"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                            "Micro đang bận, thử chạm lại sau giây lát."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                            "Chưa phát hiện giọng nói. Chạm vào khối cầu hoặc micro để nói nhé!"
                        else -> "Không thể nhận diện giọng nói (mã $error). Hãy thử lại."
                    }
                    _state.value = SpeechRecognitionState.Error(msg)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val best = matches?.firstOrNull()?.trim().orEmpty()
                    if (best.isNotEmpty()) {
                        _state.value = SpeechRecognitionState.Result(best)
                    } else {
                        _state.value = SpeechRecognitionState.Error("Không nhận được giọng nói. Chạm để nói lại!")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()
                    if (partial.isNotEmpty()) {
                        val rms = (_state.value as? SpeechRecognitionState.Listening)?.rmsDb ?: 2f
                        _state.value = SpeechRecognitionState.Listening(rms, partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            _state.value = SpeechRecognitionState.Listening(0f, "")
            sr.startListening(intent)
        } catch (e: Exception) {
            stopAndDestroy()
            _state.value = SpeechRecognitionState.Error(
                e.localizedMessage ?: "Không thể khởi động micro nhận diện giọng nói."
            )
        }
    }

    fun stopListening() {
        val sr = recognizer
        if (sr == null) {
            _state.value = SpeechRecognitionState.Idle
            return
        }
        try {
            sr.stopListening()
        } catch (_: Exception) {
            _state.value = SpeechRecognitionState.Idle
        }
    }

    fun resetState() {
        _state.value = SpeechRecognitionState.Idle
    }

    fun stopAndDestroy() {
        try {
            recognizer?.cancel()
            recognizer?.destroy()
        } catch (_: Exception) {
        }
        recognizer = null
    }
}
