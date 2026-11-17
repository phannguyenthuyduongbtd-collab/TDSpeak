package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffectsManager
import com.example.audio.TtsManager
import com.example.data.local.VocabEntity
import com.example.data.repository.VocabRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GameType(val label: String) {
    QUIZ("Quiz 4 đáp án"),
    FILL_BLANK("Điền từ"),
    DICTATION("Nghe & Gõ"),
    SCRAMBLE("Xếp chữ")
}

enum class QuizDirection {
    EN_TO_VI,
    VI_TO_EN
}

data class QuizQuestion(
    val target: VocabEntity,
    val promptText: String,
    val promptSub: String,
    val options: List<String>,
    val correctOptionIndex: Int
)

data class FillBlankQuestion(
    val target: VocabEntity,
    val maskedSentence: String,
    val fullSentence: String
)

data class LetterTile(
    val id: Int,
    val char: Char
)

data class GamesUiState(
    val selectedGame: GameType = GameType.QUIZ,
    val vocabPool: List<VocabEntity> = emptyList(),
    val isMuted: Boolean = false,

    // Quiz 4 options
    val quizDirection: QuizDirection = QuizDirection.EN_TO_VI,
    val quizQuestions: List<QuizQuestion> = emptyList(),
    val quizIndex: Int = 0,
    val quizScore: Int = 0,
    val quizStreak: Int = 0,
    val quizSelectedOption: Int? = null,
    val quizTimedOut: Boolean = false,
    val quizSecondsLeft: Int = 15,
    val quizFinished: Boolean = false,

    // Fill in the blanks
    val fillQuestions: List<FillBlankQuestion> = emptyList(),
    val fillIndex: Int = 0,
    val fillInput: String = "",
    val fillSubmittedCorrect: Boolean? = null,
    val fillScore: Int = 0,
    val fillFinished: Boolean = false,

    // Dictation
    val dictationWords: List<VocabEntity> = emptyList(),
    val dictationIndex: Int = 0,
    val dictationInput: String = "",
    val dictationShowHint: Boolean = false,
    val dictationSubmittedCorrect: Boolean? = null,
    val dictationScore: Int = 0,
    val dictationFinished: Boolean = false,

    // Word Scramble
    val scrambleWords: List<VocabEntity> = emptyList(),
    val scrambleIndex: Int = 0,
    val scrambleAvailableTiles: List<LetterTile> = emptyList(),
    val scramblePlacedTiles: List<LetterTile> = emptyList(),
    val scrambleSubmittedCorrect: Boolean? = null,
    val scrambleScore: Int = 0,
    val scrambleFinished: Boolean = false
)

class GamesViewModel(
    private val repository: VocabRepository,
    private val ttsManager: TtsManager,
    private val soundEffects: SoundEffectsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    private var quizTimerJob: Job? = null
    private var initializedGames = false

    init {
        viewModelScope.launch {
            repository.allVocab.collect { words ->
                _uiState.update { it.copy(vocabPool = words) }
                if (!initializedGames && words.isNotEmpty()) {
                    initializedGames = true
                    startAllGamesFresh(words)
                }
            }
        }
    }

    fun selectGame(game: GameType) {
        _uiState.update { it.copy(selectedGame = game) }
        when (game) {
            GameType.QUIZ -> {
                val s = _uiState.value
                if (!s.quizFinished && s.quizSelectedOption == null && !s.quizTimedOut && s.quizQuestions.isNotEmpty()) {
                    startQuizTimer()
                }
            }
            GameType.DICTATION -> {
                quizTimerJob?.cancel()
                val s = _uiState.value
                if (!s.dictationFinished && s.dictationSubmittedCorrect == null) {
                    s.dictationWords.getOrNull(s.dictationIndex)?.let {
                        ttsManager.speak(it.word, 1.0f)
                    }
                }
            }
            else -> {
                quizTimerJob?.cancel()
            }
        }
    }

    fun toggleMute() {
        val next = !_uiState.value.isMuted
        soundEffects.isMuted = next
        _uiState.update { it.copy(isMuted = next) }
    }

    private fun startAllGamesFresh(pool: List<VocabEntity>) {
        restartQuiz(pool)
        restartFillBlank(pool)
        restartDictation(pool, autoPlay = false)
        restartScramble(pool)
    }

    // ---------------- 1. QUIZ 4 LUA CHON ----------------
    fun toggleQuizDirection(direction: QuizDirection) {
        _uiState.update { it.copy(quizDirection = direction) }
        restartQuiz(_uiState.value.vocabPool)
    }

    fun restartQuiz(pool: List<VocabEntity> = _uiState.value.vocabPool) {
        quizTimerJob?.cancel()
        if (pool.size < 4) {
            _uiState.update { it.copy(quizQuestions = emptyList(), quizFinished = false) }
            return
        }
        val dir = _uiState.value.quizDirection
        val selected = pool.shuffled().take(10)
        val questions = selected.map { target ->
            val distractors = pool.filter { it.id != target.id }.shuffled().take(3)
            val allOptions = (listOf(target) + distractors).shuffled()
            val correctIdx = allOptions.indexOfFirst { it.id == target.id }
            if (dir == QuizDirection.EN_TO_VI) {
                QuizQuestion(
                    target = target,
                    promptText = target.word,
                    promptSub = "${target.partOfSpeech} ${target.phonetic}".trim(),
                    options = allOptions.map { it.meaningVi },
                    correctOptionIndex = correctIdx
                )
            } else {
                QuizQuestion(
                    target = target,
                    promptText = target.meaningVi,
                    promptSub = "(${target.partOfSpeech})",
                    options = allOptions.map { it.word },
                    correctOptionIndex = correctIdx
                )
            }
        }
        _uiState.update {
            it.copy(
                quizQuestions = questions,
                quizIndex = 0,
                quizScore = 0,
                quizStreak = 0,
                quizSelectedOption = null,
                quizTimedOut = false,
                quizSecondsLeft = 15,
                quizFinished = false
            )
        }
        if (_uiState.value.selectedGame == GameType.QUIZ) {
            startQuizTimer()
        }
    }

    private fun startQuizTimer() {
        quizTimerJob?.cancel()
        quizTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val s = _uiState.value
                if (s.selectedGame != GameType.QUIZ || s.quizFinished || s.quizSelectedOption != null || s.quizTimedOut) {
                    break
                }
                if (s.quizSecondsLeft <= 1) {
                    soundEffects.playWrong()
                    _uiState.update {
                        it.copy(
                            quizSecondsLeft = 0,
                            quizTimedOut = true,
                            quizStreak = 0
                        )
                    }
                    break
                } else {
                    _uiState.update { it.copy(quizSecondsLeft = it.quizSecondsLeft - 1) }
                }
            }
        }
    }

    fun selectQuizOption(optionIndex: Int) {
        val s = _uiState.value
        val q = s.quizQuestions.getOrNull(s.quizIndex) ?: return
        if (s.quizSelectedOption != null || s.quizTimedOut) return

        quizTimerJob?.cancel()
        val isCorrect = optionIndex == q.correctOptionIndex
        if (isCorrect) soundEffects.playCorrect() else soundEffects.playWrong()

        _uiState.update {
            it.copy(
                quizSelectedOption = optionIndex,
                quizScore = if (isCorrect) it.quizScore + 10 else it.quizScore,
                quizStreak = if (isCorrect) it.quizStreak + 1 else 0
            )
        }
    }

    fun nextQuizQuestion() {
        val s = _uiState.value
        if (s.quizIndex + 1 >= s.quizQuestions.size) {
            soundEffects.playComplete()
            _uiState.update { it.copy(quizFinished = true) }
        } else {
            _uiState.update {
                it.copy(
                    quizIndex = it.quizIndex + 1,
                    quizSelectedOption = null,
                    quizTimedOut = false,
                    quizSecondsLeft = 15
                )
            }
            startQuizTimer()
        }
    }

    // ---------------- 2. DIEN TU KHUYET ----------------
    fun restartFillBlank(pool: List<VocabEntity> = _uiState.value.vocabPool) {
        if (pool.isEmpty()) return
        val questions = pool.shuffled().take(10).map { vocab ->
            val regex = Regex("\\b${Regex.escape(vocab.word)}\\b", RegexOption.IGNORE_CASE)
            val hasWordInSentence = regex.containsMatchIn(vocab.exampleSentence)
            val masked = if (hasWordInSentence) {
                regex.replace(vocab.exampleSentence, "________")
            } else {
                "The English word meaning \"${vocab.meaningVi}\" (${vocab.partOfSpeech}) is ________."
            }
            val full = if (hasWordInSentence) {
                vocab.exampleSentence
            } else {
                "The English word meaning \"${vocab.meaningVi}\" is ${vocab.word}."
            }
            FillBlankQuestion(target = vocab, maskedSentence = masked, fullSentence = full)
        }
        _uiState.update {
            it.copy(
                fillQuestions = questions,
                fillIndex = 0,
                fillInput = "",
                fillSubmittedCorrect = null,
                fillScore = 0,
                fillFinished = false
            )
        }
    }

    fun updateFillInput(text: String) {
        if (_uiState.value.fillSubmittedCorrect != null) return
        _uiState.update { it.copy(fillInput = text) }
    }

    fun submitFillBlank() {
        val s = _uiState.value
        val q = s.fillQuestions.getOrNull(s.fillIndex) ?: return
        if (s.fillSubmittedCorrect != null || s.fillInput.isBlank()) return
        val isCorrect = s.fillInput.trim().equals(q.target.word.trim(), ignoreCase = true)
        if (isCorrect) soundEffects.playCorrect() else soundEffects.playWrong()
        _uiState.update {
            it.copy(
                fillSubmittedCorrect = isCorrect,
                fillScore = if (isCorrect) it.fillScore + 10 else it.fillScore
            )
        }
    }

    fun nextFillBlank() {
        val s = _uiState.value
        if (s.fillIndex + 1 >= s.fillQuestions.size) {
            soundEffects.playComplete()
            _uiState.update { it.copy(fillFinished = true) }
        } else {
            _uiState.update {
                it.copy(
                    fillIndex = it.fillIndex + 1,
                    fillInput = "",
                    fillSubmittedCorrect = null
                )
            }
        }
    }

    // ---------------- 3. NGHE & GO (DICTATION) ----------------
    fun restartDictation(pool: List<VocabEntity> = _uiState.value.vocabPool, autoPlay: Boolean = true) {
        if (pool.isEmpty()) return
        val words = pool.shuffled().take(10)
        _uiState.update {
            it.copy(
                dictationWords = words,
                dictationIndex = 0,
                dictationInput = "",
                dictationShowHint = false,
                dictationSubmittedCorrect = null,
                dictationScore = 0,
                dictationFinished = false
            )
        }
        if (autoPlay) {
            words.firstOrNull()?.let { ttsManager.speak(it.word, 1.0f) }
        }
    }

    fun playDictationAudio(slow: Boolean) {
        val word = _uiState.value.dictationWords.getOrNull(_uiState.value.dictationIndex)?.word ?: return
        ttsManager.speak(word, rate = if (slow) 0.62f else 1.0f)
    }

    fun toggleDictationHint() {
        _uiState.update { it.copy(dictationShowHint = !it.dictationShowHint) }
    }

    fun updateDictationInput(text: String) {
        if (_uiState.value.dictationSubmittedCorrect != null) return
        _uiState.update { it.copy(dictationInput = text) }
    }

    fun submitDictation() {
        val s = _uiState.value
        val target = s.dictationWords.getOrNull(s.dictationIndex) ?: return
        if (s.dictationSubmittedCorrect != null || s.dictationInput.isBlank()) return
        val isCorrect = s.dictationInput.trim().equals(target.word.trim(), ignoreCase = true)
        if (isCorrect) soundEffects.playCorrect() else soundEffects.playWrong()
        _uiState.update {
            it.copy(
                dictationSubmittedCorrect = isCorrect,
                dictationScore = if (isCorrect) it.dictationScore + 10 else it.dictationScore
            )
        }
    }

    fun nextDictation() {
        val s = _uiState.value
        if (s.dictationIndex + 1 >= s.dictationWords.size) {
            soundEffects.playComplete()
            _uiState.update { it.copy(dictationFinished = true) }
        } else {
            val nextIdx = s.dictationIndex + 1
            _uiState.update {
                it.copy(
                    dictationIndex = nextIdx,
                    dictationInput = "",
                    dictationShowHint = false,
                    dictationSubmittedCorrect = null
                )
            }
            s.dictationWords.getOrNull(nextIdx)?.let { ttsManager.speak(it.word, 1.0f) }
        }
    }

    // ---------------- 4. XEP CHU (WORD SCRAMBLE) ----------------
    fun restartScramble(pool: List<VocabEntity> = _uiState.value.vocabPool) {
        val eligible = pool.filter { it.word.replace(" ", "").length >= 3 }
        if (eligible.isEmpty()) return
        val words = eligible.shuffled().take(10)
        val firstTiles = buildScrambledTiles(words.first().word)
        _uiState.update {
            it.copy(
                scrambleWords = words,
                scrambleIndex = 0,
                scrambleAvailableTiles = firstTiles,
                scramblePlacedTiles = emptyList(),
                scrambleSubmittedCorrect = null,
                scrambleScore = 0,
                scrambleFinished = false
            )
        }
    }

    private fun buildScrambledTiles(word: String): List<LetterTile> {
        val chars = word.trim().lowercase().filter { it.isLetter() }.toList()
        val tiles = chars.mapIndexed { idx, c -> LetterTile(id = idx, char = c) }
        var shuffled = tiles.shuffled()
        if (chars.distinct().size > 1 && shuffled.map { it.char } == chars) {
            shuffled = tiles.reversed()
        }
        return shuffled
    }

    fun pickScrambleTile(tile: LetterTile) {
        val s = _uiState.value
        if (s.scrambleSubmittedCorrect != null) return
        _uiState.update {
            it.copy(
                scrambleAvailableTiles = it.scrambleAvailableTiles.filterNot { t -> t.id == tile.id },
                scramblePlacedTiles = it.scramblePlacedTiles + tile
            )
        }
    }

    fun removePlacedTile(tile: LetterTile) {
        val s = _uiState.value
        if (s.scrambleSubmittedCorrect != null) return
        _uiState.update {
            it.copy(
                scramblePlacedTiles = it.scramblePlacedTiles.filterNot { t -> t.id == tile.id },
                scrambleAvailableTiles = it.scrambleAvailableTiles + tile
            )
        }
    }

    fun reshuffleAvailableTiles() {
        val s = _uiState.value
        if (s.scrambleSubmittedCorrect != null) return
        _uiState.update {
            it.copy(scrambleAvailableTiles = it.scrambleAvailableTiles.shuffled())
        }
    }

    fun hintOneScrambleLetter() {
        val s = _uiState.value
        if (s.scrambleSubmittedCorrect != null) return
        val targetWord = s.scrambleWords.getOrNull(s.scrambleIndex)?.word
            ?.lowercase()?.filter { it.isLetter() } ?: return
        // Reset mismatched suffix if needed and place next correct letter
        val currentPlaced = s.scramblePlacedTiles
        var matchingPrefix = 0
        while (matchingPrefix < currentPlaced.size &&
            matchingPrefix < targetWord.length &&
            currentPlaced[matchingPrefix].char == targetWord[matchingPrefix]
        ) {
            matchingPrefix++
        }
        if (matchingPrefix >= targetWord.length) return

        val keptPlaced = currentPlaced.take(matchingPrefix)
        val returnedToPool = currentPlaced.drop(matchingPrefix)
        val pool = s.scrambleAvailableTiles + returnedToPool
        val neededChar = targetWord[matchingPrefix]
        val nextTile = pool.firstOrNull { it.char == neededChar } ?: return

        _uiState.update {
            it.copy(
                scramblePlacedTiles = keptPlaced + nextTile,
                scrambleAvailableTiles = pool.filterNot { t -> t.id == nextTile.id }
            )
        }
    }

    fun submitScramble() {
        val s = _uiState.value
        val target = s.scrambleWords.getOrNull(s.scrambleIndex) ?: return
        if (s.scrambleSubmittedCorrect != null || s.scramblePlacedTiles.isEmpty()) return

        val expected = target.word.lowercase().filter { it.isLetter() }
        val built = s.scramblePlacedTiles.joinToString("") { it.char.toString() }
        val isCorrect = built.equals(expected, ignoreCase = true)
        if (isCorrect) soundEffects.playCorrect() else soundEffects.playWrong()
        _uiState.update {
            it.copy(
                scrambleSubmittedCorrect = isCorrect,
                scrambleScore = if (isCorrect) it.scrambleScore + 10 else it.scrambleScore
            )
        }
    }

    fun nextScramble() {
        val s = _uiState.value
        if (s.scrambleIndex + 1 >= s.scrambleWords.size) {
            soundEffects.playComplete()
            _uiState.update { it.copy(scrambleFinished = true) }
        } else {
            val nextIdx = s.scrambleIndex + 1
            val nextWord = s.scrambleWords[nextIdx]
            _uiState.update {
                it.copy(
                    scrambleIndex = nextIdx,
                    scrambleAvailableTiles = buildScrambledTiles(nextWord.word),
                    scramblePlacedTiles = emptyList(),
                    scrambleSubmittedCorrect = null
                )
            }
        }
    }

    fun speakText(text: String) {
        ttsManager.speak(text, 1.0f)
    }

    override fun onCleared() {
        super.onCleared()
        quizTimerJob?.cancel()
    }
}
