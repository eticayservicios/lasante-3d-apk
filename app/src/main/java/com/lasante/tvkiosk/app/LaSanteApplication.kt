package com.lasante.tvkiosk.app

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.data.remote.RetrofitCatalogRepository
import com.lasante.tvkiosk.media.Media3AudioPlayer

// El repositorio y audioPlayer viven aquí — una sola instancia durante toda la app.
// Para volver al mock (sin internet): reemplazar RetrofitCatalogRepository() por MockCatalogRepository()
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
            .build()

    override fun onTerminate() {
        super.onTerminate()
        audioPlayer.release()
    }
}
