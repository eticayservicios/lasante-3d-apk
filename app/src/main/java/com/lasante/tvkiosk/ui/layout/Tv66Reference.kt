package com.lasante.tvkiosk.ui.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Canvas de referencia **TV66** = panel Hikvision definitivo.
 *
 * Medición real (Productos DEBUG):
 * `1280×720 · TV_LARGE · large=true · btn≈42.6dp · filter=30.8dp`
 *
 * Emulador: [scripts/run-tv66.sh] → skin `1280x720` @ dens 160.
 */
object Tv66Reference {
    val Width: Dp = 1280.dp
    val Height: Dp = 720.dp

    val MinWidthForLargeTier: Dp = 1400.dp

    /**
     * Hikvision / emulador TV66: ~1280×720.
     * Usa `.value` (Float) para evitar sorpresas en comparaciones Dp.
     */
    fun matchesReferenceCanvas(width: Dp, height: Dp): Boolean {
        val w = width.value
        val h = height.value
        return w > h && w >= 1180f && w <= 1380f && h >= 640f && h <= 800f
    }

    fun forceTvLargeTier(width: Dp, height: Dp, preferTv66: Boolean = false): Boolean =
        preferTv66 || matchesReferenceCanvas(width, height) || width.value > MinWidthForLargeTier.value
}
