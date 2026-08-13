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
    const val GIRA_GIF = "file:///android_asset/vitrina/ui/gira.gif"
    const val TOUCH_GIF = "file:///android_asset/vitrina/ui/touch.gif"
    const val BADGE_HISTORIA = "file:///android_asset/vitrina/ui/badge_historia.png"
    const val FILTER_BUTTON = "file:///android_asset/vitrina/ui/filter_button.png"

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

    /**
     * PNG de filtro (cuadrado 512×512, recortado al círculo).
     * Decode acotado + cache key versionada (invalidar assets viejos con padding).
     */
    fun filterRequest(context: Context): ImageRequest =
        ImageRequest.Builder(context)
            .data(FILTER_BUTTON)
            .size(512, 512)
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCacheKey("filter_button_v2_512")
            .diskCacheKey("filter_button_v2_512")
            .build()
}
