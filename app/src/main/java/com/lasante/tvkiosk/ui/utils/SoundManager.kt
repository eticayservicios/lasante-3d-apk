package com.lasante.tvkiosk.ui.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import com.lasante.tvkiosk.R

/**
 * Sonidos de UI desde [R.raw].
 *
 * Mapa (carpeta `MP3 Sonidos/`):
 * - [UiSound.Click] — historia, estrellas, redes, retroceso
 *   (`virtual_vibes` / [R.raw.click_sound_new]; el denielcz del pack dura ~50 ms y no se oye)
 * - [UiSound.Unit] — tap unidad de negocio en vitrina
 * - [UiSound.Product] — seleccionar producto (grid / burbujas)
 * - [UiSound.Error] — pantallas de error
 */
enum class UiSound(@RawRes val resId: Int) {
    Click(R.raw.click_sound_new),
    Unit(R.raw.unit_tap),
    Product(R.raw.ui_tap),
    Error(R.raw.ui_error),
}

object SoundManager {

    fun play(context: Context, sound: UiSound) {
        try {
            MediaPlayer.create(context, sound.resId)?.apply {
                setOnCompletionListener { release() }
                start()
            }
        } catch (_: Exception) {
            // Silenciar errores de audio en kiosco
        }
    }

    /** Click compartido (historia / estrellas / redes / retroceso). */
    fun playClickSound(context: Context) = play(context, UiSound.Click)

    fun playUnitSound(context: Context) = play(context, UiSound.Unit)

    fun playProductSound(context: Context) = play(context, UiSound.Product)

    fun playErrorSound(context: Context) = play(context, UiSound.Error)
}
