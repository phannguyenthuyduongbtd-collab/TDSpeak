package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabDao {
    @Query("SELECT * FROM vocab_words ORDER BY createdAt DESC, id DESC")
    fun getAllVocab(): Flow<List<VocabEntity>>

    @Query("SELECT * FROM vocab_words WHERE nextReviewAt <= :nowMillis ORDER BY nextReviewAt ASC")
    fun getDueVocab(nowMillis: Long): Flow<List<VocabEntity>>

    @Query("SELECT COUNT(*) FROM vocab_words")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vocab: VocabEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vocabs: List<VocabEntity>)

    @Update
    suspend fun update(vocab: VocabEntity)

    @Query("DELETE FROM vocab_words WHERE id = :id")
    suspend fun deleteById(id: Long)
}
