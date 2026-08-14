package com.lasante.tvkiosk.ui.layout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lasante.tvkiosk.BuildConfig

/**
 * Canvas de referencia **tablet Ariana** / Television_1080 forzado.
 *
 * Medición: [scripts/run-tv1080-ariana.sh] → `wm size 1137x711` @ dens 160.
 * Perfil esperado: TV_REGULAR · tv_42 · largeCanvas.
 */
object ArianaTabletReference {
    val Width: Dp = 1137.dp
    val Height: Dp = 711.dp
}

/**
 * Fuerza **toda** la app a verse como la tablet de Ariana (1137×711).
 *
 * ON (DEBUG):
 * - Viewport lógico [ArianaTabletReference] 1137×711, escalado para llenar el host
 * - Perfil natural: TV_REGULAR / tv_42 (no tv_66)
 *
 * Mutuamente exclusivo con [HikvisionLayoutDebug]: si hikForce está ON, este se ignora.
 *
 * Activar/desactivar solo en [LayoutForceLocal] (archivo local, gitignored).
 */
object ArianaLayoutDebug {
    fun isForced(): Boolean =
        BuildConfig.DEBUG &&
            LayoutForceLocal.FORCE_ARIANA_LAYOUT &&
            !HikvisionLayoutDebug.isForced()

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
            "ArianaLayout",
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
            "arianaForce=ON·1137×711·scale=$scaleTxt$hostTxt"
        } else {
            "arianaForce=off"
        }
}

/** Label DEBUG del force activo (Hikvision o Ariana). */
fun layoutForceOverlayLabel(): String = when {
    HikvisionLayoutDebug.isForced() -> HikvisionLayoutDebug.overlayLabel()
    ArianaLayoutDebug.isForced() -> ArianaLayoutDebug.overlayLabel()
    else -> "layoutForce=off"
}
