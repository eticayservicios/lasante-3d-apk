package com.lasante.tvkiosk.ui.utils

import android.content.Context
import android.media.MediaPlayer
import com.lasante.tvkiosk.R

object SoundManager {
    
    fun playClickSound(context: Context) {
        try {
            MediaPlayer.create(context, R.raw.click_sound_new)?.apply {
                setOnCompletionListener { release() }
                start()
            }
        } catch (e: Exception) {
            // Silenciar cualquier error de audio
        }
    }
}
