package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audio.SoundEffectsManager
import com.example.audio.TtsManager
import com.example.data.local.TDSpeakDatabase
import com.example.data.repository.VocabRepository
import com.example.ui.TDSpeakApp
import com.example.ui.theme.TDSpeakTheme
import com.example.viewmodel.GamesViewModel
import com.example.viewmodel.SpeakingViewModel
import com.example.viewmodel.VocabViewModel

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: TtsManager
    private lateinit var soundEffectsManager: SoundEffectsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ttsManager = TtsManager(applicationContext)
        soundEffectsManager = SoundEffectsManager()
        val database = TDSpeakDatabase.getInstance(applicationContext)
        val repository = VocabRepository(database.vocabDao())

        val speakingViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SpeakingViewModel(application, ttsManager) as T
                }
            }
        )[SpeakingViewModel::class.java]

        val vocabViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VocabViewModel(repository, ttsManager) as T
                }
            }
        )[VocabViewModel::class.java]

        val gamesViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GamesViewModel(repository, ttsManager, soundEffectsManager) as T
                }
            }
        )[GamesViewModel::class.java]

        setContent {
            TDSpeakTheme {
                TDSpeakApp(
                    speakingViewModel = speakingViewModel,
                    vocabViewModel = vocabViewModel,
                    gamesViewModel = gamesViewModel
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            ttsManager.release()
            soundEffectsManager.release()
        }
    }
}
