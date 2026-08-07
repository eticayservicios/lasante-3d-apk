package com.lasante.tvkiosk.ui.layout

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Detecta paneles TV grandes ~65–75" / 4K (Hikvision, Android TV 4K).
 *
 * Perfiles:
 * - **tv_42 / Fire / 1080p**: canvas ~960×540 dp (o tablet similar). NO usar tv_66.
 * - **tv_66**: canvas muy ancho (>1400 dp), TV leanback 4K, Hikvision, o 4K
 *   “comprimido” por densidad alta (≥400 dpi con ≥2560 px).
 *
 * Importante: tablets 2K (p. ej. 2560×1600 @ 240–320 dpi) NO son tv_66;
 * si no, se ven “todo granote” como el bug del emulador 1080 mal tipado.
 */
object TvProfileDetector {

    fun isTv66Candidate(
        maxWidth: Dp,
        maxHeight: Dp,
        density: Density,
        context: Context,
    ): Boolean {
        // Canvas Compose ya enorme (p. ej. 4K @ dens 320 → ~1920 dp).
        if (maxWidth > 1400.dp) return true

        val isTvSized = maxWidth >= 880.dp && maxHeight >= 480.dp && maxWidth > maxHeight
        if (!isTvSized) return false

        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val longSidePx = maxOf(widthPx, heightPx)
        val densityDpi = density.density * 160f

        // 4K nativo, o 4K reportando ~960 dp por dens ≥400 (kiosco/TV).
        val isTrue4k = longSidePx >= 3840f
        val isDensityCompressed4k = longSidePx >= 2560f && densityDpi >= 400f

        // Paneles comerciales Hikvision (siempre tv_66 en tamaño TV).
        if (isHikvisionDevice()) return true

        // Android TV / leanback solo si es 4K de verdad (no Full HD 1920).
        if (isTelevisionUi(context) && (isTrue4k || isDensityCompressed4k)) return true

        // Kiosco 4K sin leanback (densidad alta). NO aplica a tablets 2K @ 240–320 dpi.
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
