package com.example

import com.example.audio.PronunciationEvaluator
import com.example.audio.SentenceSplitter
import com.example.audio.WordMatchStatus
import com.example.data.local.VocabDao
import com.example.data.local.VocabEntity
import com.example.data.repository.SrsRating
import com.example.data.repository.VocabRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TDSpeakCoreTest {

    @Test
    fun pronunciationEvaluator_exactMatchScores100() {
        val result = PronunciationEvaluator.evaluate(
            targetSentence = "We must protect our natural environment.",
            spokenTranscript = "we must protect our natural environment"
        )
        assertEquals(100, result.score)
        assertTrue(result.wordEvaluations.all { it.status == WordMatchStatus.CORRECT })
        assertTrue(result.missedWords.isEmpty())
    }

    @Test
    fun pronunciationEvaluator_detectsMissedAndNearWords() {
        val result = PronunciationEvaluator.evaluate(
            targetSentence = "Students can recycle paper waste.",
            spokenTranscript = "Students recycle paper wastes"
        )
        assertEquals(WordMatchStatus.CORRECT, result.wordEvaluations[0].status)
        assertEquals(WordMatchStatus.MISSED_OR_WRONG, result.wordEvaluations[1].status) // "can" missed
        assertEquals(WordMatchStatus.CORRECT, result.wordEvaluations[2].status)
        assertEquals(WordMatchStatus.CORRECT, result.wordEvaluations[3].status)
        assertEquals(WordMatchStatus.NEAR_MATCH, result.wordEvaluations[4].status) // "waste" vs "wastes"
        assertTrue(result.score in 65..85)
    }

    @Test
    fun sentenceSplitter_preservesAbbreviationsAndSplitsParagraph() {
        val text = "Dr. Smith teaches English at our school. We practice speaking every day! Do you enjoy it?"
        val sentences = SentenceSplitter.splitIntoShortSentences(text)
        assertEquals(3, sentences.size)
        assertEquals("Dr. Smith teaches English at our school.", sentences[0])
        assertEquals("We practice speaking every day!", sentences[1])
        assertEquals("Do you enjoy it?", sentences[2])
    }

    @Test
    fun srsAlgorithm_assignsExpectedIntervals() = runTest {
        val fakeDao = FakeVocabDao()
        val repo = VocabRepository(fakeDao)
        val word = VocabEntity(
            id = 1L,
            word = "confidence",
            partOfSpeech = "noun",
            phonetic = "/ˈkɒnfɪdəns/",
            meaningVi = "sự tự tin",
            exampleSentence = "Practice builds confidence."
        )
        fakeDao.insert(word)

        repo.applySrsReview(word, SrsRating.FORGOT)
        val afterForgot = fakeDao.latest()!!
        assertEquals("1 phút", afterForgot.intervalLabel)
        assertEquals(1, afterForgot.intervalMinutes)
        assertEquals(0, afterForgot.repetitions)

        repo.applySrsReview(afterForgot, SrsRating.VAGUE)
        val afterVague = fakeDao.latest()!!
        assertEquals("1 ngày", afterVague.intervalLabel)
        assertEquals(1440, afterVague.intervalMinutes)
        assertEquals(1, afterVague.repetitions)

        repo.applySrsReview(afterVague, SrsRating.MASTERED)
        val afterMastered = fakeDao.latest()!!
        assertEquals("5 ngày", afterMastered.intervalLabel)
        assertEquals(2, afterMastered.repetitions)

        repo.applySrsReview(afterMastered, SrsRating.MASTERED)
        val afterSecondMastered = fakeDao.latest()!!
        assertEquals("7 ngày", afterSecondMastered.intervalLabel)
    }

    private class FakeVocabDao : VocabDao {
        private val items = MutableStateFlow<List<VocabEntity>>(emptyList())
        override fun getAllVocab(): Flow<List<VocabEntity>> = items
        override fun getDueVocab(nowMillis: Long): Flow<List<VocabEntity>> = items
        override suspend fun getCount(): Int = items.value.size
        override suspend fun insert(vocab: VocabEntity): Long {
            items.value = items.value + vocab
            return vocab.id
        }
        override suspend fun insertAll(vocabs: List<VocabEntity>) {
            items.value = items.value + vocabs
        }
        override suspend fun update(vocab: VocabEntity) {
            items.value = items.value.map { if (it.id == vocab.id) vocab else it }
        }
        override suspend fun deleteById(id: Long) {
            items.value = items.value.filterNot { it.id == id }
        }
        fun latest(): VocabEntity? = items.value.firstOrNull()
    }
}
