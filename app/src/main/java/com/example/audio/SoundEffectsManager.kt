package com.example.audio

import android.media.AudioManager
import android.media.ToneGenerator

class SoundEffectsManager {
    private val toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 65)
    } catch (_: Exception) {
        null
    }

    var isMuted: Boolean = false

    fun playCorrect() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 160)
        } catch (_: Exception) {
        }
    }

    fun playWrong() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 240)
        } catch (_: Exception) {
        }
    }

    fun playComplete() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 260)
        } catch (_: Exception) {
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
        } catch (_: Exception) {
        }
    }
}
