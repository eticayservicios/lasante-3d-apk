package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Capa de interacción unificada sobre el cilindro (todos los perfiles).
 * Swipe en cualquier zona → arrastre; tap dentro del cintillo → unidad activa.
 */
@Composable
fun VitrinaActiveUnitTapLayer(
    activeIndex: Int,
    projectedUnits: Map<Int, FeaturedSlotScreenPoint>,
    enabled: Boolean,
    tapEnabled: Boolean,
    metrics: IntroLayoutMetrics,
    dragSlopPx: Float,
    onUnitClick: (String) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!enabled) return

    val density = LocalDensity.current
    val glbIndex = VitrinaGlbMapping.glbIndexFor(activeIndex)
    val targetUnitId = VitrinaGlbMapping.navigationUnitIdFor(glbIndex)
    val hotspotWidth = cintilloHotspotWidth(metrics)
    val hotspotHeight = cintilloHotspotHeight(metrics)
    val yFraction = cintilloBandYFraction(metrics)

    BoxWithConstraints(modifier = modifier) {
        val areaWidthPx = with(density) { maxWidth.toPx() }
        val areaHeightPx = with(density) { maxHeight.toPx() }
        val hotspotWpx = with(density) { hotspotWidth.toPx() }
        val hotspotHpx = with(density) { hotspotHeight.toPx() }
        val cintilloLeft = areaWidthPx / 2f - hotspotWpx / 2f
        val cintilloTop = areaHeightPx * yFraction - hotspotHpx / 2f
        val cintilloRight = cintilloLeft + hotspotWpx
        val cintilloBottom = cintilloTop + hotspotHpx

        Box(
            modifier = Modifier
                .fillMaxSize()
                .vitrinaTapOrHorizontalDragGesture(
                    dragSlopPx = dragSlopPx,
                    onTap = { pos ->
                        if (!tapEnabled) return@vitrinaTapOrHorizontalDragGesture
                        val inCintillo =
                            pos.x in cintilloLeft..cintilloRight &&
                                pos.y in cintilloTop..cintilloBottom
                        if (!inCintillo) return@vitrinaTapOrHorizontalDragGesture
                        android.util.Log.d(
                            "VitrinaTap",
                            "CINTILLO_TAP activeIndex=$activeIndex glbIndex=$glbIndex " +
                                "targetUnitId=$targetUnitId yFraction=$yFraction",
                        )
                        onUnitClick(targetUnitId)
                    },
                    onDragStart = onDragStart,
                    onDrag = onDrag,
                    onDragEnd = onDragEnd,
                ),
        )
    }
}

private fun cintilloHotspotWidth(metrics: IntroLayoutMetrics): Dp = when {
    metrics.isPhoneLandscape -> 162.dp
    metrics.isTv42 || metrics.isTabletLandscape -> 260.dp
    metrics.isTv32 || metrics.isTv66 -> 240.dp
    else -> 200.dp
}

private fun cintilloHotspotHeight(metrics: IntroLayoutMetrics): Dp = when {
    metrics.isPhoneLandscape -> 53.dp
    metrics.isTv42 || metrics.isTabletLandscape -> 84.dp
    metrics.isTv32 || metrics.isTv66 -> 72.dp
    else -> 60.dp
}

private fun cintilloBandYFraction(metrics: IntroLayoutMetrics): Float = when {
    metrics.isTv42 || metrics.isTabletLandscape -> 0.88f
    metrics.isTv32 || metrics.isTv66 -> 0.86f
    metrics.isPhoneLandscape -> 0.82f
    else -> 0.84f
}
