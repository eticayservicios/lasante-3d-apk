package com.lasante.tvkiosk.ui.layout

import com.lasante.tvkiosk.BuildConfig

/**
 * Fuerza **toda** la app a verse como el panel Hikvision TV66.
 *
 * ON (DEBUG):
 * - Perfil / métricas TV_LARGE + tv_66
 * - Viewport fijo [Tv66Reference] 1280×720 (letterbox en Damasco, etc.)
 *
 * OFF o release: perfil y canvas reales del dispositivo.
 *
 * Cambiar [FORCE_HIKVISION_LAYOUT] a `false` para desactivar.
 */
object HikvisionLayoutDebug {
    /** ON = ver toda la app como Hikvision TV66 (viewport 1280×720). */
    const val FORCE_HIKVISION_LAYOUT: Boolean = true

    fun isForced(): Boolean = BuildConfig.DEBUG && FORCE_HIKVISION_LAYOUT

    /** Texto corto para overlays DEBUG. */
    fun overlayLabel(): String =
        if (isForced()) "hikForce=ON·1280×720" else "hikForce=off"
}
