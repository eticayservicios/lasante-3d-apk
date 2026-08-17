package com.lasante.tvkiosk.ui.layout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import com.lasante.tvkiosk.BuildConfig

/**
 * Fuerza **toda** la app a verse como TV66.
 *
 * ON (DEBUG):
 * - Perfil / métricas TV_LARGE + tv_66
 * - Viewport lógico [Tv66Reference] 1280×720, escalado para llenar el host
 *
 * OFF o release: perfil y canvas reales del dispositivo.
 *
 * Activar/desactivar solo en [LayoutForceLocal] (archivo local, gitignored).
 */
object Tv66LayoutDebug {
    fun isForced(): Boolean =
        BuildConfig.DEBUG && LayoutForceLocal.FORCE_TV66_LAYOUT

    /** Escala aplicada (host / 1280×720). En tablet large ≈ 1.04. */
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
            "Tv66Layout",
            "host=${previewHostW.toInt()}×${previewHostH.toInt()} " +
                "ref=1280×720 scale=${"%.3f".format(previewScale)} " +
                "forced=${isForced()}",
        )
    }

    /** Texto corto para overlays DEBUG. */
    fun overlayLabel(): String =
        if (isForced()) {
            val scaleTxt = "%.2f".format(previewScale)
            val hostTxt = if (previewHostW > 0f) {
                "·host=${previewHostW.toInt()}×${previewHostH.toInt()}"
            } else {
                ""
            }
            "tv66Force=ON·1280×720·scale=$scaleTxt$hostTxt"
        } else {
            "tv66Force=off"
        }
}
