package com.lasante.tvkiosk.ui.layout

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Detecta paneles TV grandes / 4K (p. ej. Hikvision, Android TV 4K) que reportan
 * ~960×540 dp por densidad alta (320–640) pero son físicamente 1920–3840 px.
 *
 * Sin esto, caen en `tv_42` y no aplican métricas `tv_66`.
 */
object TvProfileDetector {

    fun isTv66Candidate(
        maxWidth: Dp,
        maxHeight: Dp,
        density: Density,
        context: Context,
    ): Boolean {
        if (maxWidth > 1400.dp) return true

        val isTvSized = maxWidth >= 880.dp && maxHeight >= 480.dp && maxWidth > maxHeight
        if (!isTvSized) return false

        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val longSidePx = maxOf(widthPx, heightPx)
        val shortSidePx = minOf(widthPx, heightPx)
        val is4kPhysical = longSidePx >= 2560f || (longSidePx >= 1920f && shortSidePx >= 1000f)

        if (isHikvisionDevice() && isTvSized) return true
        if (isTelevisionUi(context) && is4kPhysical) return true
        // Panel kiosco 4K sin leanback (algunos SO comerciales).
        if (is4kPhysical && longSidePx >= 2560f) return true

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
