package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.PronunciationEvaluator
import com.example.audio.PronunciationResult
import com.example.audio.SentenceSplitter
import com.example.audio.SpeechRecognitionState
import com.example.audio.SpeechRecognizerManager
import com.example.audio.TtsManager
import com.example.data.speaking.SpeakingPromptItem
import com.example.data.speaking.SpeakingTopic
import com.example.data.speaking.SpeakingTopicsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SpeakingMode {
    TOPICS,
    CUSTOM_DOCUMENT
}

data class CustomSentenceItem(
    val id: String,
    val text: String
)

data class SpeakingUiState(
    val mode: SpeakingMode = SpeakingMode.TOPICS,
    val topics: List<SpeakingTopic> = SpeakingTopicsData.topics,
    val selectedTopicId: String = SpeakingTopicsData.topics.first().id,
    val selectedPromptId: String = SpeakingTopicsData.topics.first().prompts.first().id,
    val activeTargetId: String? = null,
    val activeTargetSentence: String = "",
    val speechState: SpeechRecognitionState = SpeechRecognitionState.Idle,
    val resultsByTargetId: Map<String, PronunciationResult> = emptyMap(),
    val customRawText: String = DEFAULT_CUSTOM_PASSAGE,
    val customSentences: List<CustomSentenceItem> = emptyList(),
    val documentLoadError: String? = null,
    val isTtsSpeaking: Boolean = false
) {
    val selectedTopic: SpeakingTopic
        get() = topics.firstOrNull { it.id == selectedTopicId } ?: topics.first()

    val selectedPrompt: SpeakingPromptItem
        get() = selectedTopic.prompts.firstOrNull { it.id == selectedPromptId }
            ?: selectedTopic.prompts.first()
}

private const val DEFAULT_CUSTOM_PASSAGE =
    "Learning English every day helps students gain confidence. Short speaking sessions improve both fluency and pronunciation. Never be afraid of making mistakes because every practice makes you better!"

class SpeakingViewModel(
    application: Application,
    val ttsManager: TtsManager
) : AndroidViewModel(application) {

    private val speechRecognizer = SpeechRecognizerManager(application)

    private val _uiState = MutableStateFlow(
        SpeakingUiState(
            customSentences = splitToItems(DEFAULT_CUSTOM_PASSAGE)
        )
    )
    val uiState: StateFlow<SpeakingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            speechRecognizer.state.collect { recState ->
                _uiState.update { state ->
                    if (recState is SpeechRecognitionState.Result && state.activeTargetId != null) {
                        val eval = PronunciationEvaluator.evaluate(
                            targetSentence = state.activeTargetSentence,
                            spokenTranscript = recState.transcript
                        )
                        state.copy(
                            speechState = recState,
                            resultsByTargetId = state.resultsByTargetId + (state.activeTargetId to eval)
                        )
                    } else {
                        state.copy(speechState = recState)
                    }
                }
            }
        }

        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isTtsSpeaking = speaking) }
            }
        }
    }

    fun selectMode(mode: SpeakingMode) {
        stopRecordingIfActive()
        _uiState.update { it.copy(mode = mode, activeTargetId = null) }
    }

    fun selectTopic(topicId: String) {
        stopRecordingIfActive()
        val topic = _uiState.value.topics.firstOrNull { it.id == topicId } ?: return
        _uiState.update {
            it.copy(
                selectedTopicId = topic.id,
                selectedPromptId = topic.prompts.first().id,
                activeTargetId = null
            )
        }
    }

    fun selectPrompt(promptId: String) {
        stopRecordingIfActive()
        _uiState.update { it.copy(selectedPromptId = promptId, activeTargetId = null) }
    }

    fun speakText(text: String, slow: Boolean = false) {
        ttsManager.speak(text, rate = if (slow) 0.72f else 1.0f)
    }

    fun startRecordingForTarget(targetId: String, targetSentence: String) {
        ttsManager.stop()
        val currentSpeech = _uiState.value.speechState
        if (currentSpeech is SpeechRecognitionState.Listening && _uiState.value.activeTargetId == targetId) {
            speechRecognizer.stopListening()
            return
        }
        _uiState.update {
            it.copy(
                activeTargetId = targetId,
                activeTargetSentence = targetSentence
            )
        }
        speechRecognizer.startListening()
    }

    fun stopRecordingIfActive() {
        speechRecognizer.stopAndDestroy()
        speechRecognizer.resetState()
    }

    fun clearResultForTarget(targetId: String) {
        _uiState.update {
            it.copy(
                resultsByTargetId = it.resultsByTargetId - targetId,
                speechState = SpeechRecognitionState.Idle
            )
        }
    }

    fun updateCustomRawText(text: String) {
        _uiState.update {
            it.copy(
                customRawText = text,
                documentLoadError = null
            )
        }
    }

    fun splitCustomPassage() {
        val raw = _uiState.value.customRawText
        val items = splitToItems(raw)
        _uiState.update {
            it.copy(
                customSentences = items,
                documentLoadError = if (items.isEmpty()) "Vui lòng nhập hoặc dán đoạn văn tiếng Anh trước khi tách câu." else null
            )
        }
    }

    fun loadDocumentFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader().readText()
                    }.orEmpty()
                }
                if (content.isBlank()) {
                    _uiState.update {
                        it.copy(documentLoadError = "Tệp trống hoặc không hỗ trợ định dạng văn bản thuần (.txt).")
                    }
                } else {
                    val items = splitToItems(content)
                    _uiState.update {
                        it.copy(
                            customRawText = content.take(4000),
                            customSentences = items,
                            documentLoadError = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(documentLoadError = "Không thể đọc tệp đã chọn. Hãy thử tệp .txt hoặc dán trực tiếp.")
                }
            }
        }
    }

    private fun splitToItems(rawText: String): List<CustomSentenceItem> {
        return SentenceSplitter.splitIntoShortSentences(rawText).mapIndexed { index, s ->
            CustomSentenceItem(id = "custom_$index", text = s)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.stopAndDestroy()
    }
}
