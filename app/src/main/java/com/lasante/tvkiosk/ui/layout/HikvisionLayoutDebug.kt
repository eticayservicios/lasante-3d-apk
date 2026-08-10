package com.lasante.tvkiosk.ui.layout

import com.lasante.tvkiosk.BuildConfig

/**
 * Fuerza métricas / perfil **TV66 (Hikvision)** en **toda** la app
 * (Intro, CT, Productos, modales), útil en Damasco u otros paneles
 * para validar el layout del panel físico.
 *
 * Solo aplica en builds **DEBUG**. En release siempre está off.
 *
 * Cambiar [FORCE_HIKVISION_LAYOUT] a `false` para volver al perfil real del dispositivo.
 */
object HikvisionLayoutDebug {
    /** ON = ver toda la app como Hikvision TV66. */
    const val FORCE_HIKVISION_LAYOUT: Boolean = true

    fun isForced(): Boolean = BuildConfig.DEBUG && FORCE_HIKVISION_LAYOUT

    /** Texto corto para overlays DEBUG. */
    fun overlayLabel(): String =
        if (isForced()) "hikForce=ON" else "hikForce=off"
}
