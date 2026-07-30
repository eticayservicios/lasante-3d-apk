package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.data.VitrinaUnit
import kotlin.math.roundToInt

@Composable
fun BusinessUnitVitrina(
    metrics: IntroLayoutMetrics,
    vitrinaUnits: List<VitrinaUnit>,
    activeIndex: Int,
    displayRotationDegrees: Float,
    isDragging: Boolean,
    baseRotationHandle: VitrinaBaseRotationHandle,
    vitrinaFilamentSession: VitrinaFilamentSession,
    showProducts: Boolean,
    renderScene: Boolean = true,
    sceneActive: Boolean = true,
    filamentRenderingEnabled: Boolean = true,
    backdropBlurred: Boolean = false,
    vitrinaInteractionEnabled: Boolean = true,
    showOverlayControls: Boolean = true,
    rotationAnimationSpec: AnimationSpec<Float> = VitrinaConstants.manualRotationAnimationSpec,
    onProductClick: (Product) -> Unit,
    onWakeFromIdle: () -> Unit,
    onRotationAnimationFinished: () -> Unit = {},
    onUnitClick: (String) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onRotateClick: () -> Unit = {},
) {
    val active = resolveActiveVitrinaUnit(vitrinaUnits, activeIndex) ?: return
    val (glbIndex, activeVitrinaUnit) = active
    val unitProducts = featuredProductsForUnit(activeVitrinaUnit)
    val featuredSlotProducts = productsByVisualSlot(unitProducts)
    val sceneHeight = metrics.maxHeight * metrics.sceneHeightFraction
    val cylinderNudge = metrics.vitrinaCylinderNudgeDown
    val verticalOffsetPx = with(LocalDensity.current) {
        (metrics.maxHeight * metrics.vitrinaVerticalBias +
            metrics.vitrinaVerticalOffsetAdjustment +
            cylinderNudge).roundToPx()
    }
    // Las burbujas no bajan con el cilindro: cancelan el nudge.
    val bubblesCounterOffsetPx = with(LocalDensity.current) { (-cylinderNudge).roundToPx() }
    var projectedUnits by remember { mutableStateOf<Map<Int, FeaturedSlotScreenPoint>>(emptyMap()) }
    val bubblesVisible = showProducts && vitrinaInteractionEnabled
    val unitTapEnabled = bubblesVisible && !isDragging
    val density = LocalDensity.current
    val dragPointerSlopPx = metrics.dragPointerSlopPx(density)

    LaunchedEffect(activeVitrinaUnit.unit.id, glbIndex, unitProducts) {
        VitrinaDebugLog.d(
            "VitrinaBubbles",
            "unit=${activeVitrinaUnit.unit.id} glbIndex=$glbIndex products=${unitProducts.size}",
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VitrinaProductosEstrellasBadge(
            visible = bubblesVisible,
            metrics = metrics,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -metrics.bubblesBadgeTopPullUp)
                .zIndex(12f),
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset { IntOffset(0, verticalOffsetPx) }
                .fillMaxWidth(metrics.sceneWidthFraction)
                .height(sceneHeight),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
            ) {
                Box(
                    modifier = Modifier
                        .width(metrics.dragHandleWidth)
                        .fillMaxHeight()
                        .then(
                            if (vitrinaInteractionEnabled && showProducts) {
                                Modifier.vitrinaEdgeDragGesture(
                                    dragSlopPx = dragPointerSlopPx,
                                    onDragStart = { onWakeFromIdle(); onDragStart() },
                                    onDrag = onDrag,
                                    onDragEnd = onDragEnd,
                                )
                            } else {
                                Modifier
                            },
                        ),
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    VitrinaBubblesRow(
                        slotProducts = featuredSlotProducts,
                        visible = bubblesVisible,
                        metrics = metrics,
                        onProductClick = { product ->
                            onWakeFromIdle()
                            onProductClick(product)
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset { IntOffset(0, bubblesCounterOffsetPx) }
                            .padding(top = metrics.bubblesRowTopInScene)
                            .zIndex(12f),
                    )

                    if (metrics.hasStableLayout) {
                        VitrinaModelViewer(
                            activeUnitId = activeVitrinaUnit.unit.id,
                            activeIndex = activeIndex,
                            displayRotationDegrees = displayRotationDegrees,
                            baseRotationHandle = baseRotationHandle,
                            filamentSession = vitrinaFilamentSession,
                            rotationAnimationSpec = rotationAnimationSpec,
                            layoutMetrics = metrics,
                            sceneActive = sceneActive && renderScene,
                            filamentRenderingEnabled = filamentRenderingEnabled && renderScene,
                            backdropBlurred = backdropBlurred,
                            isDragging = isDragging,
                            onUnitAnchorCentersChanged = { projectedUnits = it },
                            onRotationAnimationFinished = onRotationAnimationFinished,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    if (vitrinaInteractionEnabled && showProducts) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(5f)
                                .vitrinaBodyDragGesture(
                                    dragSlopPx = dragPointerSlopPx,
                                    onDragStart = { onWakeFromIdle(); onDragStart() },
                                    onDrag = onDrag,
                                    onDragEnd = onDragEnd,
                                ),
                        )

                        VitrinaActiveUnitTapLayer(
                            activeIndex = activeIndex,
                            projectedUnits = projectedUnits,
                            enabled = unitTapEnabled,
                            metrics = metrics,
                            onUnitClick = { unitId ->
                                onWakeFromIdle()
                                onUnitClick(unitId)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(6f),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(metrics.dragHandleWidth)
                        .fillMaxHeight()
                        .then(
                            if (vitrinaInteractionEnabled && showProducts) {
                                Modifier.vitrinaEdgeDragGesture(
                                    dragSlopPx = dragPointerSlopPx,
                                    onDragStart = { onWakeFromIdle(); onDragStart() },
                                    onDrag = onDrag,
                                    onDragEnd = onDragEnd,
                                )
                            } else {
                                Modifier
                            },
                        ),
                )
            }

            // Gira: anclado al borde derecho de la escena (no a la columna del cilindro),
            // para que endPadding/protrude sí se noten en TV42.
            if (showOverlayControls) {
                IntroActionButton(
                    assetPath = "file:///android_asset/vitrina/ui/gira.gif",
                    onClick = onRotateClick,
                    size = metrics.rotateButtonSize,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = metrics.rotateButtonEndPadding)
                        .offset(
                            x = metrics.rotateButtonProtrudeOffset,
                            y = metrics.rotateButtonCenterYOffset,
                        )
                        .zIndex(40f),
                )
            }

            if (showOverlayControls && vitrinaInteractionEnabled && showProducts) {
                IntroActionButton(
                    assetPath = "file:///android_asset/vitrina/ui/touch.gif",
                    onClick = {
                        onWakeFromIdle()
                        onUnitClick(activeVitrinaUnit.unit.id)
                    },
                    size = metrics.rotateButtonSize,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = metrics.rotateButtonEndPadding,
                            bottom = metrics.touchHintBottomPadding,
                        )
                        .offset(x = metrics.rotateButtonProtrudeOffset)
                        .zIndex(40f),
                )
            }
            // Historia: BadgeHistoria + gif en IntroLandscapeLayout (rail derecho).
        }
    }
}
