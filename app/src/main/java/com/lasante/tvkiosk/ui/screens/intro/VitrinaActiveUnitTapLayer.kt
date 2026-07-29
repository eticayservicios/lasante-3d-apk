package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Hotspot del cintillo (base del cilindro, donde se lee Hospital Care / etc.).
 *
 * Calibrado con el GLB: banda visual ≈ Y glTF -0.1 / slot_categoria Y≈0.05 →
 * fracción de pantalla ~0.80 en el área 3D (no en los estantes).
 *
 * Los nombres de unidad en el GLB no reciben el toque solos: el clic es este hitbox Compose.
 */
@Composable
fun VitrinaActiveUnitTapLayer(
    activeIndex: Int,
    projectedUnits: Map<Int, FeaturedSlotScreenPoint>,
    enabled: Boolean,
    metrics: IntroLayoutMetrics,
    onUnitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!enabled) return

    val glbIndex = VitrinaGlbMapping.glbIndexFor(activeIndex)
    val targetUnitId = VitrinaGlbMapping.navigationUnitIdFor(glbIndex)

    val hotspotWidth = cintilloHotspotWidth(metrics)
    val hotspotHeight = cintilloHotspotHeight(metrics)
    val yFraction = cintilloBandYFraction(metrics)

    BoxWithConstraints(modifier = modifier) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = maxWidth * 0.5f - hotspotWidth / 2,
                    y = maxHeight * yFraction - hotspotHeight / 2,
                )
                .size(width = hotspotWidth, height = hotspotHeight)
                .clickable(
                    interactionSource = remember(targetUnitId, activeIndex) {
                        MutableInteractionSource()
                    },
                    indication = null,
                ) {
                    android.util.Log.d(
                        "VitrinaTap",
                        "CINTILLO_TAP activeIndex=$activeIndex glbIndex=$glbIndex " +
                            "targetUnitId=$targetUnitId yFraction=$yFraction",
                    )
                    onUnitClick(targetUnitId)
                },
        )
    }
}

private fun cintilloHotspotWidth(metrics: IntroLayoutMetrics): Dp = when {
    metrics.isPhoneLandscape -> 162.dp // +1.4% sobre 160
    metrics.isTv32 || metrics.isTv42 || metrics.isTv66 -> 193.dp // +1.4% sobre 190
    else -> 172.dp
}

private fun cintilloHotspotHeight(metrics: IntroLayoutMetrics): Dp = when {
    metrics.isPhoneLandscape -> 53.dp // +1.4% sobre 52
    metrics.isTv32 || metrics.isTv42 || metrics.isTv66 -> 57.dp // +1.4% sobre 56
    else -> 55.dp
}

/**
 * Fracción Y del área 3D (0=arriba, 1=abajo).
 * Derivado del GLB: centro de banda ≈ -0.1 en espacio modelo → ~0.80–0.82 en proyección.
 */
private fun cintilloBandYFraction(metrics: IntroLayoutMetrics): Float = when {
    // TV42/tablet: el cilindro baja con nudge; la banda “Specialty Care” queda más abajo.
    metrics.isTv42 || metrics.isTabletLandscape -> 0.88f
    metrics.isTv32 || metrics.isTv66 -> 0.86f
    metrics.isPhoneLandscape -> 0.82f
    else -> 0.84f
}
