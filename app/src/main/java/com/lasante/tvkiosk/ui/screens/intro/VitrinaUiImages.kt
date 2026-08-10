package com.lasante.tvkiosk.ui.screens.intro

import android.content.Context
import coil.request.CachePolicy
import coil.request.ImageRequest

/**
 * Assets UI de vitrina + ImageRequest único (GIF/PNG) con cache Coil.
 * Evita duplicar Builder en Historia / gira / touch.
 */
object VitrinaUiImages {
    const val HISTORIA_GIF = "file:///android_asset/vitrina/ui/Historia.gif"
    const val BADGE_HISTORIA = "file:///android_asset/vitrina/ui/badge_historia.png"

    fun request(
        context: Context,
        data: String,
        isGif: Boolean = data.endsWith(".gif", ignoreCase = true),
    ): ImageRequest =
        ImageRequest.Builder(context)
            .data(data)
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .apply {
                // Evita frames basura / flash blanco en GIFs con disposal (Infinix / mid-range).
                if (isGif) allowHardware(false)
            }
            .build()
}
