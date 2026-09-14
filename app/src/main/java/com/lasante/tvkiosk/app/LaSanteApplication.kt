package com.lasante.tvkiosk.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.memory.MemoryCache
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.data.remote.RetrofitCatalogRepository
import com.lasante.tvkiosk.media.Media3AudioPlayer

// El repositorio y audioPlayer viven aquí — una sola instancia durante toda la app.
class LaSanteApplication : Application(), ImageLoaderFactory {

    val catalogRepository: CatalogRepository by lazy {
        RetrofitCatalogRepository()
    }

    val audioPlayer: Media3AudioPlayer by lazy {
        Media3AudioPlayer().also { it.attach(this) }
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components {
                // Forzamos GifDecoder.Factory() en todas las versiones de Android para evitar
                // destellos blancos o loops corruptos en GIFs transparentes (bug de ImageDecoderDecoder)
                add(GifDecoder.Factory())
            }
            // Historia/gira/touch: cache acotado. 0.28 + gira.gif full-res explotaba RAM al volver de idle.
            .memoryCache {
                val am = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
                val percent = when {
                    am.isLowRamDevice || am.memoryClass <= 192 -> 0.12
                    am.memoryClass <= 256 -> 0.18
                    else -> 0.22
                }
                MemoryCache.Builder(this)
                    .maxSizePercent(percent)
                    .build()
            }
            .build()

    override fun onTerminate() {
        super.onTerminate()
        audioPlayer.release()
    }
}
