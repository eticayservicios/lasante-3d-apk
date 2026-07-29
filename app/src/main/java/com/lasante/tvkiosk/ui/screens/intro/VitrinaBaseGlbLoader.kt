package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

data class VitrinaBaseGlbSource(
    val loadPath: String,
    val fromAssets: Boolean,
)

/**
 * El GLB base siempre desde assets del APK.
 * La carga por ruta de caché (`fileLocation`) falla en dispositivo con el Draco de ~26 MB;
 * el asset empaquetado sí carga (mismo archivo que CloudFront).
 */
@Composable
fun rememberVitrinaBaseGlbSource(): VitrinaBaseGlbSource =
    remember {
        VitrinaBaseGlbSource(
            loadPath = VitrinaConstants.BASE_GLB_ASSET,
            fromAssets = true,
        )
    }
