package com.lasante.tvkiosk.ui.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
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

/**
 * SFX de UI con [SoundPool] reutilizable (evita crear [android.media.MediaPlayer]
 * por cada click en el hilo principal).
 */
object SoundManager {
    private const val TAG = "SoundManager"
    private const val MAX_STREAMS = 4

    @Volatile
    private var soundPool: SoundPool? = null

    private val loadedIds = mutableMapOf<Int, Int>()
    private val loadLock = Any()

    private fun pool(context: Context): SoundPool {
        soundPool?.let { return it }
        synchronized(loadLock) {
            soundPool?.let { return it }
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            return SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(attrs)
                .build()
                .also { created ->
                    soundPool = created
                    UiSound.entries.forEach { sound ->
                        val id = created.load(context.applicationContext, sound.resId, 1)
                        loadedIds[sound.resId] = id
                    }
                }
        }
    }

    fun play(context: Context, sound: UiSound) {
        try {
            val sp = pool(context.applicationContext)
            val sampleId = synchronized(loadLock) { loadedIds[sound.resId] } ?: return
            if (sampleId == 0) return
            sp.play(sampleId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            Log.w(TAG, "play failed for ${sound.name}", e)
        }
    }

    /** Click compartido (historia / estrellas / redes / retroceso). */
    fun playClickSound(context: Context) = play(context, UiSound.Click)

    fun playUnitSound(context: Context) = play(context, UiSound.Unit)

    fun playProductSound(context: Context) = play(context, UiSound.Product)

    fun playErrorSound(context: Context) = play(context, UiSound.Error)

    /** Liberar al destruir el proceso (opcional). */
    fun release() {
        synchronized(loadLock) {
            soundPool?.release()
            soundPool = null
            loadedIds.clear()
        }
    }
}
