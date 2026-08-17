package com.lasante.tvkiosk.ui.layout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lasante.tvkiosk.BuildConfig

/**
 * Canvas de referencia **TV1080** / Television_1080 forzado.
 *
 * Medición: [scripts/run-tv1080.sh] → `wm size 1137x711` @ dens 160.
 * Perfil esperado: TV_REGULAR · tv_42 · largeCanvas.
 */
object Tv1080Reference {
    val Width: Dp = 1137.dp
    val Height: Dp = 711.dp

    /**
     * TV1080 / Television_1080 forzado: ~1137×711.
     * Usa `.value` (Float) para evitar sorpresas en comparaciones Dp.
     */
    fun matchesReferenceCanvas(width: Dp, height: Dp): Boolean {
        val w = width.value
        val h = height.value
        return w > h && w >= 1050f && w <= 1220f && h >= 650f && h <= 780f
    }
}

/**
 * Fuerza **toda** la app a verse como la TV1080 (1137×711).
 *
 * ON (DEBUG):
 * - Viewport lógico [Tv1080Reference] 1137×711, escalado para llenar el host
 * - Perfil natural: TV_REGULAR / tv_42 (no tv_66)
 *
 * Mutuamente exclusivo con [Tv66LayoutDebug]: si tv66Force está ON, este se ignora.
 *
 * Activar/desactivar solo en [LayoutForceLocal] (archivo local, gitignored).
 */
object Tv1080LayoutDebug {
    fun isForced(): Boolean =
        BuildConfig.DEBUG &&
            LayoutForceLocal.FORCE_TV1080_LAYOUT &&
            !Tv66LayoutDebug.isForced()

    var previewScale: Float by mutableFloatStateOf(1f)
        private set
    var previewHostW: Float by mutableFloatStateOf(0f)
        private set
    var previewHostH: Float by mutableFloatStateOf(0f)
        private set

    fun updatePreviewHost(
        hostWidthDp: Float,
        hostHeightDp: Float,
        appliedScale: Float,
    ) {
        previewScale = appliedScale
        previewHostW = hostWidthDp
        previewHostH = hostHeightDp
        android.util.Log.i(
            "Tv1080Layout",
            "host=${previewHostW.toInt()}×${previewHostH.toInt()} " +
                "ref=1137×711 scale=${"%.3f".format(previewScale)} " +
                "forced=${isForced()}",
        )
    }

    fun overlayLabel(): String =
        if (isForced()) {
            val scaleTxt = "%.2f".format(previewScale)
            val hostTxt = if (previewHostW > 0f) {
                "·host=${previewHostW.toInt()}×${previewHostH.toInt()}"
            } else {
                ""
            }
            "tv1080Force=ON·1137×711·scale=$scaleTxt$hostTxt"
        } else {
            "tv1080Force=off"
        }
}

/** Label DEBUG del force activo (TV66 o TV1080). */
fun layoutForceOverlayLabel(): String = when {
    Tv66LayoutDebug.isForced() -> Tv66LayoutDebug.overlayLabel()
    Tv1080LayoutDebug.isForced() -> Tv1080LayoutDebug.overlayLabel()
    else -> "layoutForce=off"
}
