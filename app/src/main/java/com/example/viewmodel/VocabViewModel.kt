package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.TtsManager
import com.example.data.local.VocabEntity
import com.example.data.repository.SrsRating
import com.example.data.repository.VocabRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class VocabTabSection {
    FLASHCARD_SRS,
    NOTEBOOK
}

enum class VocabFilter {
    ALL,
    DUE,
    MASTERED
}

data class VocabUiState(
    val section: VocabTabSection = VocabTabSection.FLASHCARD_SRS,
    val allWords: List<VocabEntity> = emptyList(),
    val filteredWords: List<VocabEntity> = emptyList(),
    val dueWords: List<VocabEntity> = emptyList(),
    val masteredCount: Int = 0,
    val searchQuery: String = "",
    val filter: VocabFilter = VocabFilter.ALL,
    val isCardFlipped: Boolean = false,
    val practiceAllMode: Boolean = false,
    val currentCardIndex: Int = 0,
    val reviewedTodayCount: Int = 0,
    val editingWord: VocabEntity? = null,
    val isAddEditSheetOpen: Boolean = false
) {
    val activeDeck: List<VocabEntity>
        get() = if (practiceAllMode) allWords else dueWords

    val currentFlashcard: VocabEntity?
        get() {
            val deck = activeDeck
            if (deck.isEmpty()) return null
            return deck[currentCardIndex.coerceIn(0, deck.lastIndex)]
        }
}

private data class VocabControlState(
    val section: VocabTabSection = VocabTabSection.FLASHCARD_SRS,
    val searchQuery: String = "",
    val filter: VocabFilter = VocabFilter.ALL,
    val isCardFlipped: Boolean = false,
    val practiceAllMode: Boolean = false,
    val currentCardIndex: Int = 0,
    val reviewedTodayCount: Int = 0,
    val editingWord: VocabEntity? = null,
    val isAddEditSheetOpen: Boolean = false
)

class VocabViewModel(
    private val repository: VocabRepository,
    private val ttsManager: TtsManager
) : ViewModel() {

    private val controlState = MutableStateFlow(VocabControlState())
    private val clockTick = MutableStateFlow(System.currentTimeMillis())

    val uiState: StateFlow<VocabUiState> = combine(
        repository.allVocab,
        controlState,
        clockTick
    ) { words, ctrl, now ->
        val due = words.filter { it.nextReviewAt <= now }
            .sortedBy { it.nextReviewAt }
        val mastered = words.count { it.easeLevel >= 2 && it.nextReviewAt > now }

        val q = ctrl.searchQuery.trim().lowercase()
        val filtered = words.filter { word ->
            val matchesQuery = q.isEmpty() ||
                word.word.lowercase().contains(q) ||
                word.meaningVi.lowercase().contains(q) ||
                word.partOfSpeech.lowercase().contains(q)
            val matchesFilter = when (ctrl.filter) {
                VocabFilter.ALL -> true
                VocabFilter.DUE -> word.nextReviewAt <= now
                VocabFilter.MASTERED -> word.easeLevel >= 2 && word.nextReviewAt > now
            }
            matchesQuery && matchesFilter
        }

        VocabUiState(
            section = ctrl.section,
            allWords = words,
            filteredWords = filtered,
            dueWords = due,
            masteredCount = mastered,
            searchQuery = ctrl.searchQuery,
            filter = ctrl.filter,
            isCardFlipped = ctrl.isCardFlipped,
            practiceAllMode = ctrl.practiceAllMode,
            currentCardIndex = ctrl.currentCardIndex,
            reviewedTodayCount = ctrl.reviewedTodayCount,
            editingWord = ctrl.editingWord,
            isAddEditSheetOpen = ctrl.isAddEditSheetOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VocabUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
        // Periodically refresh current time so 1-minute "Quên / Khó" cards reappear automatically
        viewModelScope.launch {
            while (true) {
                delay(15_000L)
                clockTick.value = System.currentTimeMillis()
            }
        }
    }

    fun selectSection(section: VocabTabSection) {
        controlState.update { it.copy(section = section) }
    }

    fun updateSearchQuery(query: String) {
        controlState.update { it.copy(searchQuery = query) }
    }

    fun selectFilter(filter: VocabFilter) {
        controlState.update { it.copy(filter = filter) }
    }

    fun flipCard() {
        controlState.update { it.copy(isCardFlipped = !it.isCardFlipped) }
    }

    fun speakWord(text: String, slow: Boolean = false) {
        ttsManager.speak(text, rate = if (slow) 0.7f else 1.0f)
    }

    fun togglePracticeAllMode(enabled: Boolean) {
        controlState.update {
            it.copy(
                practiceAllMode = enabled,
                currentCardIndex = 0,
                isCardFlipped = false
            )
        }
    }

    fun rateCurrentCard(rating: SrsRating) {
        val current = uiState.value.currentFlashcard ?: return
        val deckSize = uiState.value.activeDeck.size
        viewModelScope.launch {
            repository.applySrsReview(current, rating)
            clockTick.value = System.currentTimeMillis()
            controlState.update { ctrl ->
                val nextIndex = if (ctrl.practiceAllMode && deckSize > 0) {
                    (ctrl.currentCardIndex + 1) % deckSize
                } else {
                    0
                }
                ctrl.copy(
                    isCardFlipped = false,
                    currentCardIndex = nextIndex,
                    reviewedTodayCount = ctrl.reviewedTodayCount + 1
                )
            }
        }
    }

    fun openAddWordDialog() {
        controlState.update { it.copy(editingWord = null, isAddEditSheetOpen = true) }
    }

    fun openEditWordDialog(word: VocabEntity) {
        controlState.update { it.copy(editingWord = word, isAddEditSheetOpen = true) }
    }

    fun closeAddEditDialog() {
        controlState.update { it.copy(editingWord = null, isAddEditSheetOpen = false) }
    }

    fun saveWord(
        id: Long,
        word: String,
        partOfSpeech: String,
        phonetic: String,
        meaningVi: String,
        exampleSentence: String
    ) {
        val existing = uiState.value.allWords.firstOrNull { it.id == id }
        val entity = if (existing != null) {
            existing.copy(
                word = word.trim(),
                partOfSpeech = partOfSpeech.trim().ifEmpty { "noun" },
                phonetic = phonetic.trim(),
                meaningVi = meaningVi.trim(),
                exampleSentence = exampleSentence.trim()
            )
        } else {
            VocabEntity(
                word = word.trim(),
                partOfSpeech = partOfSpeech.trim().ifEmpty { "noun" },
                phonetic = phonetic.trim(),
                meaningVi = meaningVi.trim(),
                exampleSentence = exampleSentence.trim(),
                nextReviewAt = System.currentTimeMillis()
            )
        }
        viewModelScope.launch {
            repository.addOrUpdate(entity)
            controlState.update { it.copy(editingWord = null, isAddEditSheetOpen = false) }
        }
    }

    fun deleteWord(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}
