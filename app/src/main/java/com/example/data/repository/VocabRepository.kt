package com.example.data.repository

import com.example.data.local.VocabDao
import com.example.data.local.VocabEntity
import kotlinx.coroutines.flow.Flow

enum class SrsRating {
    FORGOT,      // Quên / Khó -> lặp lại sau 1 phút
    VAGUE,       // Nhớ mang máng -> lặp lại sau 1 ngày
    MASTERED     // Đã thuộc lòng -> lặp lại sau 3-7 ngày
}

class VocabRepository(private val vocabDao: VocabDao) {

    val allVocab: Flow<List<VocabEntity>> = vocabDao.getAllVocab()

    suspend fun ensureSeeded() {
        if (vocabDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            vocabDao.insertAll(defaultVocabList(now))
        }
    }

    suspend fun addOrUpdate(vocab: VocabEntity) {
        if (vocab.id == 0L) {
            vocabDao.insert(vocab)
        } else {
            vocabDao.update(vocab)
        }
    }

    suspend fun deleteById(id: Long) {
        vocabDao.deleteById(id)
    }

    suspend fun applySrsReview(vocab: VocabEntity, rating: SrsRating) {
        val now = System.currentTimeMillis()
        val updated = when (rating) {
            SrsRating.FORGOT -> {
                vocab.copy(
                    nextReviewAt = now + 60_000L, // 1 phút
                    intervalMinutes = 1,
                    intervalLabel = "1 phút",
                    repetitions = 0,
                    easeLevel = 0
                )
            }
            SrsRating.VAGUE -> {
                vocab.copy(
                    nextReviewAt = now + 24L * 60L * 60_000L, // 1 ngày
                    intervalMinutes = 1440,
                    intervalLabel = "1 ngày",
                    repetitions = vocab.repetitions + 1,
                    easeLevel = 1
                )
            }
            SrsRating.MASTERED -> {
                val nextDays = when {
                    vocab.repetitions <= 0 -> 3
                    vocab.repetitions == 1 -> 5
                    else -> 7
                }
                vocab.copy(
                    nextReviewAt = now + nextDays * 24L * 60L * 60_000L, // 3-7 ngày
                    intervalMinutes = nextDays * 1440,
                    intervalLabel = "$nextDays ngày",
                    repetitions = vocab.repetitions + 1,
                    easeLevel = 2
                )
            }
        }
        vocabDao.update(updated)
    }

    private fun defaultVocabList(now: Long): List<VocabEntity> = listOf(
        VocabEntity(
            word = "environment",
            partOfSpeech = "noun",
            phonetic = "/ɪnˈvaɪrənmənt/",
            meaningVi = "môi trường",
            exampleSentence = "We must protect our natural environment for future generations.",
            nextReviewAt = now,
            createdAt = now - 16000
        ),
        VocabEntity(
            word = "confidence",
            partOfSpeech = "noun",
            phonetic = "/ˈkɒnfɪdəns/",
            meaningVi = "sự tự tin",
            exampleSentence = "Practicing every day builds my confidence when speaking English.",
            nextReviewAt = now,
            createdAt = now - 15000
        ),
        VocabEntity(
            word = "volunteer",
            partOfSpeech = "verb",
            phonetic = "/ˌvɒlənˈtɪə(r)/",
            meaningVi = "tình nguyện",
            exampleSentence = "Many students volunteer to clean the local park every Sunday.",
            nextReviewAt = now,
            createdAt = now - 14000
        ),
        VocabEntity(
            word = "sustainable",
            partOfSpeech = "adj",
            phonetic = "/səˈsteɪnəbl/",
            meaningVi = "bền vững",
            exampleSentence = "Solar power is a clean and sustainable source of energy.",
            nextReviewAt = now,
            createdAt = now - 13000
        ),
        VocabEntity(
            word = "curriculum",
            partOfSpeech = "noun",
            phonetic = "/kəˈrɪkjələm/",
            meaningVi = "chương trình học",
            exampleSentence = "Our school curriculum includes both science and communication skills.",
            nextReviewAt = now,
            createdAt = now - 12000
        ),
        VocabEntity(
            word = "collaborate",
            partOfSpeech = "verb",
            phonetic = "/kəˈlæbəreɪt/",
            meaningVi = "hợp tác, làm việc nhóm",
            exampleSentence = "Classmates often collaborate on group presentations after school.",
            nextReviewAt = now,
            createdAt = now - 11000
        ),
        VocabEntity(
            word = "enthusiastic",
            partOfSpeech = "adj",
            phonetic = "/ɪnˌθjuːziˈæstɪk/",
            meaningVi = "nhiệt huyết, hào hứng",
            exampleSentence = "She is very enthusiastic about joining the English speaking club.",
            nextReviewAt = now,
            createdAt = now - 10000
        ),
        VocabEntity(
            word = "recycle",
            partOfSpeech = "verb",
            phonetic = "/ˌriːˈsaɪkl/",
            meaningVi = "tái chế",
            exampleSentence = "Remember to recycle plastic bottles and paper at school.",
            nextReviewAt = now,
            createdAt = now - 9000
        ),
        VocabEntity(
            word = "achievement",
            partOfSpeech = "noun",
            phonetic = "/əˈtʃiːvmənt/",
            meaningVi = "thành tựu",
            exampleSentence = "Winning the debate contest was her proudest achievement this year.",
            nextReviewAt = now,
            createdAt = now - 8000
        ),
        VocabEntity(
            word = "concentrate",
            partOfSpeech = "verb",
            phonetic = "/ˈkɒnsntreɪt/",
            meaningVi = "tập trung",
            exampleSentence = "A quiet library helps me concentrate better on my homework.",
            nextReviewAt = now,
            createdAt = now - 7000
        ),
        VocabEntity(
            word = "creativity",
            partOfSpeech = "noun",
            phonetic = "/ˌkriːeɪˈtɪvəti/",
            meaningVi = "sự sáng tạo",
            exampleSentence = "Drawing and music encourage creativity among high school students.",
            nextReviewAt = now,
            createdAt = now - 6000
        ),
        VocabEntity(
            word = "fluently",
            partOfSpeech = "adv",
            phonetic = "/ˈfluːəntli/",
            meaningVi = "một cách trôi chảy",
            exampleSentence = "With daily shadowing, you can speak English much more fluently.",
            nextReviewAt = now,
            createdAt = now - 5000
        ),
        VocabEntity(
            word = "pollution",
            partOfSpeech = "noun",
            phonetic = "/pəˈluːʃn/",
            meaningVi = "sự ô nhiễm",
            exampleSentence = "Riding bicycles to school helps reduce air pollution in cities.",
            nextReviewAt = now,
            createdAt = now - 4000
        ),
        VocabEntity(
            word = "responsibility",
            partOfSpeech = "noun",
            phonetic = "/rɪˌspɒnsəˈbɪləti/",
            meaningVi = "trách nhiệm",
            exampleSentence = "Protecting nature is a shared responsibility for everyone.",
            nextReviewAt = now,
            createdAt = now - 3000
        ),
        VocabEntity(
            word = "opportunity",
            partOfSpeech = "noun",
            phonetic = "/ˌɒpəˈtjuːnəti/",
            meaningVi = "cơ hội",
            exampleSentence = "This exchange program is a great opportunity to make new friends.",
            nextReviewAt = now,
            createdAt = now - 2000
        ),
        VocabEntity(
            word = "improve",
            partOfSpeech = "verb",
            phonetic = "/ɪmˈpruːv/",
            meaningVi = "cải thiện",
            exampleSentence = "Listening to short podcasts will quickly improve your pronunciation.",
            nextReviewAt = now,
            createdAt = now - 1000
        )
    )
}
