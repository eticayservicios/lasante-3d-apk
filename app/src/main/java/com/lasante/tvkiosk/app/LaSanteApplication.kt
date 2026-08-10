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
            // Historia.gif / gira / touch: más margen de memoria para no re-decodificar al volver a Intro.
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.28)
                    .build()
            }
            .build()

    override fun onTerminate() {
        super.onTerminate()
        audioPlayer.release()
    }
}
