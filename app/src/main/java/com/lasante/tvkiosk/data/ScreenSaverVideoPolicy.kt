package com.lasante.tvkiosk.data

import android.util.Log

/** Solo videos oficiales del CDN en la playlist de screen saver. */
object ScreenSaverVideoPolicy {
    private const val TAG = "VideoCache"
    private const val ALLOWED_PREFIX = "https://catalog-assets.gurusaws.com/videos/vitrina/screen-saver/"

    fun isAllowedUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return false
        if (!trimmed.startsWith(ALLOWED_PREFIX, ignoreCase = true)) {
            Log.w(TAG, "Screen saver omitido — URL fuera del CDN oficial: $trimmed")
            return false
        }
        if (trimmed.contains("whatsapp", ignoreCase = true)) {
            Log.w(TAG, "Screen saver omitido — video de prueba: $trimmed")
            return false
        }
        if (!trimmed.endsWith(".mp4", ignoreCase = true)) {
            Log.w(TAG, "Screen saver omitido — extensión no permitida: $trimmed")
            return false
        }
        return true
    }

    fun filterPlaylist(videos: List<ScreenSaverVideo>): List<ScreenSaverVideo> =
        videos.filter { it.enabled && isAllowedUrl(it.url) }
}
