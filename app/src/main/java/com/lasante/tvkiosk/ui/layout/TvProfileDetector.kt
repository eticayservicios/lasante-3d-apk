package com.lasante.tvkiosk.ui.layout

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Detecta paneles TV grandes (perfil **tv_66** / [DeviceProfileTier.TV_LARGE]).
 *
 * Canvas de referencia (Hikvision definitivo): [Tv66Reference] → **1280×720 dp**.
 *
 * Perfiles:
 * - **tv_42 / Fire / 1080p**: canvas ~960×540 dp (o tablet similar ~1137×711). NO usar tv_66.
 * - **tv_66**: canvas ref. Hikvision 1280×720, ancho >1400 dp, Hikvision HW,
 *   TV leanback 4K, o 4K “comprimido” por densidad alta.
 *
 * Importante: tablets 2K (p. ej. 2560×1600 @ 240–320 dpi) NO son tv_66.
 */
object TvProfileDetector {

    fun isTv66Candidate(
        maxWidth: Dp,
        maxHeight: Dp,
        density: Density,
        context: Context,
    ): Boolean {
        // Debug: forzar TV66 en toda la app (Damasco → layout Hikvision).
        if (HikvisionLayoutDebug.isForced()) return true

        // Hikvision definitivo (emulador skin 1280×720 o panel real).
        if (Tv66Reference.matchesReferenceCanvas(maxWidth, maxHeight)) return true
        // Paneles Compose muy anchos.
        if (maxWidth > Tv66Reference.MinWidthForLargeTier) return true

        val isTvSized = maxWidth >= 880.dp && maxHeight >= 480.dp && maxWidth > maxHeight
        if (!isTvSized) return false

        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val longSidePx = maxOf(widthPx, heightPx)
        val densityDpi = density.density * 160f

        val isTrue4k = longSidePx >= 3840f
        val isDensityCompressed4k = longSidePx >= 2560f && densityDpi >= 400f

        if (isHikvisionDevice()) return true

        if (isTelevisionUi(context) && (isTrue4k || isDensityCompressed4k)) return true

        if (isTrue4k || isDensityCompressed4k) return true

        return false
    }

    private fun isTelevisionUi(context: Context): Boolean {
        if (context.packageManager.hasSystemFeature("android.software.leanback")) return true
        val uiMode = context.getSystemService(UiModeManager::class.java)?.currentModeType
        return uiMode == Configuration.UI_MODE_TYPE_TELEVISION
    }

    private fun isHikvisionDevice(): Boolean {
        val tokens = listOf(
            Build.MANUFACTURER,
            Build.BRAND,
            Build.MODEL,
            Build.DEVICE,
            Build.PRODUCT,
        ).joinToString(" ").lowercase()
        return tokens.contains("hikvision") ||
            tokens.contains("hiksoft") ||
            tokens.contains("hikvisiondigital") ||
            // Monitores/paneles comerciales Hikvision (serie DS-D…)
            Regex("""\bds-d""").containsMatchIn(tokens)
    }
}
