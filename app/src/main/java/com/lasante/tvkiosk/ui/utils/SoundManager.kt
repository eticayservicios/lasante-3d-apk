package com.lasante.tvkiosk.ui.utils

import android.content.Context
import android.media.MediaPlayer
import com.lasante.tvkiosk.R

/**
 * Sonido de UI ([R.raw.ui_tap]) solo para:
 * - volver a unidad de negocio (Intro),
 * - seleccionar producto en el panel de productos,
 * - seleccionar red social en la pantalla principal.
 */
object SoundManager {

    fun playClickSound(context: Context) {
        try {
            MediaPlayer.create(context, R.raw.ui_tap)?.apply {
                setOnCompletionListener { release() }
                start()
            }
        } catch (_: Exception) {
            // Silenciar cualquier error de audio
        }
    }
}
