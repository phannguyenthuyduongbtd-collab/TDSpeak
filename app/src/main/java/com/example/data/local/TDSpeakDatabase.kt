package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VocabEntity::class], version = 1, exportSchema = false)
abstract class TDSpeakDatabase : RoomDatabase() {
    abstract fun vocabDao(): VocabDao

    companion object {
        @Volatile
        private var INSTANCE: TDSpeakDatabase? = null

        fun getInstance(context: Context): TDSpeakDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TDSpeakDatabase::class.java,
                    "tdspeak_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
