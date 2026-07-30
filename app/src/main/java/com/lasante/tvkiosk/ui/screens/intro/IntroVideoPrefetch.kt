package com.lasante.tvkiosk.ui.screens.intro

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.lasante.tvkiosk.data.ScreenSaverVideo
import com.lasante.tvkiosk.data.ScreenSaverVideoPolicy
import com.lasante.tvkiosk.media.VideoCache
import kotlinx.coroutines.delay

private const val TAG = "VideoCache"

/** Prefetch de videos: screen saver pronto; institucional tras vitrina base (evita OOM). */
@Composable
fun IntroDeferredVideoPrefetch(
    vitrinaFilamentSession: VitrinaFilamentSession,
    screenSaverVideos: List<ScreenSaverVideo>,
    institutionalVideoUrl: String?,
    enabled: Boolean = true,
) {
    val appContext = LocalContext.current.applicationContext
    val configuration = LocalConfiguration.current
    val profile = remember(configuration) {
        VitrinaDeviceLoadPolicy.resolve(appContext, configuration)
    }
    val baseLoaded = vitrinaFilamentSession.baseInstance != null
    val screenSaverUrlsKey = remember(screenSaverVideos) {
        ScreenSaverVideoPolicy.filterPlaylist(screenSaverVideos).joinToString("|") { it.url.trim() }
    }

    LaunchedEffect(enabled, profile, screenSaverUrlsKey, baseLoaded) {
        if (!enabled || screenSaverUrlsKey.isBlank()) return@LaunchedEffect
        // Phone: no prefetch hasta tener el GLB base en memoria estable.
        val phoneLike = !profile.isAndroidTv && !profile.isTabletLandscape
        if (phoneLike && !baseLoaded) return@LaunchedEffect

        val delayMs = VitrinaDeviceLoadPolicy.screenSaverPrefetchDelayMs(profile)
        if (delayMs == null) {
            Log.i(TAG, "Prefetch screen saver omitido en emulador")
            return@LaunchedEffect
        }

        Log.i(
            TAG,
            "Prefetch screen saver en ${delayMs / 1000}s " +
                "(tv=${profile.isAndroidTv} tablet=${profile.isTabletLandscape} " +
                "lowRam=${profile.isLowRamDevice} baseLoaded=$baseLoaded)",
        )
        delay(delayMs)
        VideoCache.prefetchScreenSaverVideos(
            context = appContext,
            screenSaverVideos = screenSaverVideos,
        )
    }

    LaunchedEffect(enabled, baseLoaded, profile, institutionalVideoUrl) {
        if (!enabled || !baseLoaded || institutionalVideoUrl.isNullOrBlank()) return@LaunchedEffect

        val delayMs = VitrinaDeviceLoadPolicy.institutionalVideoPrefetchDelayMs(profile)
        if (delayMs == null) {
            Log.i(TAG, "Prefetch video institucional omitido en emulador")
            return@LaunchedEffect
        }

        Log.i(TAG, "Vitrina base lista — prefetch institucional en ${delayMs / 1000}s")
        delay(delayMs)
        VideoCache.prefetchUrls(
            context = appContext,
            urls = listOf(institutionalVideoUrl),
            label = "institucional",
        )
    }
}
