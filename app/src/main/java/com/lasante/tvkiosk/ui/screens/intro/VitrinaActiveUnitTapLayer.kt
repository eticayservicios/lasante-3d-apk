package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.lasante.tvkiosk.ui.utils.SoundManager

/**
 * Capa de interacción unificada sobre el cilindro (todos los perfiles).
 *
 * - Swipe en cualquier zona → arrastre / rotación.
 * - Tap en la cara frontal (estantes + cintillo, “cuadro rojo”) → unidad activa.
 * Las burbujas van en zIndex más alto y siguen capturando sus propios taps.
 */
@Composable
fun VitrinaActiveUnitTapLayer(
    activeIndex: Int,
    activeUnitId: String,
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
    val context = LocalContext.current
    val glbIndex = VitrinaGlbMapping.glbIndexFor(activeIndex)
    val mappedUnitId = VitrinaGlbMapping.navigationUnitIdFor(glbIndex)
    // Tap = ID de la cara activeIndex (1:1 con GLB), no el unitId suelto del API.
    val targetUnitId = mappedUnitId
    val topFrac = unitTapTopFraction(metrics)
    val bottomFrac = unitTapBottomFraction(metrics)
    val widthFrac = unitTapWidthFraction(metrics)

    BoxWithConstraints(modifier = modifier) {
        val areaWidthPx = with(density) { maxWidth.toPx() }
        val areaHeightPx = with(density) { maxHeight.toPx() }
        val tapWidthPx = areaWidthPx * widthFrac
        val tapLeft = areaWidthPx * 0.5f - tapWidthPx / 2f
        val tapRight = tapLeft + tapWidthPx
        val tapTop = areaHeightPx * topFrac
        val tapBottom = areaHeightPx * bottomFrac

        Box(
            modifier = Modifier
                .fillMaxSize()
                .vitrinaTapOrHorizontalDragGesture(
                    dragSlopPx = dragSlopPx,
                    onTap = { pos ->
                        if (!tapEnabled) return@vitrinaTapOrHorizontalDragGesture
                        val inUnitFace =
                            pos.x in tapLeft..tapRight &&
                                pos.y in tapTop..tapBottom
                        if (!inUnitFace) return@vitrinaTapOrHorizontalDragGesture
                        android.util.Log.i(
                            "VitrinaDiag",
                            "TAP activeIndex=$activeIndex glbNode=${VitrinaGlbMapping.glbNodeNameFor(activeIndex)} " +
                                "activeUnitId=$activeUnitId mappedUnitId=$mappedUnitId → navigate=$targetUnitId",
                        )
                        SoundManager.playUnitSound(context)
                        onUnitClick(targetUnitId)
                    },
                    onDragStart = onDragStart,
                    onDrag = onDrag,
                    onDragEnd = onDragEnd,
                ),
        )
    }
}

/** Empieza bajo las burbujas / badge, sobre el primer estante visible. */
private fun unitTapTopFraction(metrics: IntroLayoutMetrics): Float = when {
    metrics.isPhoneLandscape -> 0.34f
    metrics.isTv42 || metrics.isTabletLandscape -> 0.30f
    metrics.isTv66 -> 0.32f
    else -> 0.32f
}

/** Incluye el cintillo con el nombre de la unidad. */
private fun unitTapBottomFraction(metrics: IntroLayoutMetrics): Float = when {
    metrics.isPhoneLandscape -> 0.94f
    metrics.isTv42 || metrics.isTabletLandscape -> 0.96f
    metrics.isTv66 -> 0.95f
    else -> 0.95f
}

/** Ancho de la cara frontal (~cuadro rojo sobre los estantes). */
private fun unitTapWidthFraction(metrics: IntroLayoutMetrics): Float = when {
    metrics.isPhoneLandscape -> 0.52f
    metrics.isTv42 || metrics.isTabletLandscape -> 0.46f
    metrics.isTv66 -> 0.44f
    else -> 0.48f
}
