package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocab_words")
data class VocabEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val word: String,
    val partOfSpeech: String,
    val phonetic: String,
    val meaningVi: String,
    val exampleSentence: String,
    val nextReviewAt: Long = System.currentTimeMillis(),
    val intervalMinutes: Int = 0,
    val intervalLabel: String = "Mới",
    val repetitions: Int = 0,
    val easeLevel: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
