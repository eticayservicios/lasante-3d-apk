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
import androidx.compose.ui.draw.alpha
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
    isUserActive: Boolean = true,
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
    onStarProductsClick: () -> Unit = {},
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
        android.util.Log.i(
            "VitrinaDiag",
            "ACTIVE index=$activeIndex glbIndex=$glbIndex " +
                "node=${VitrinaGlbMapping.glbNodeNameFor(activeIndex)} " +
                "unitId=${activeVitrinaUnit.unit.id} " +
                "navMapped=${VitrinaGlbMapping.navigationUnitIdFor(glbIndex)} " +
                "products=${unitProducts.size}",
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        onStarProductsClick = {
                            onWakeFromIdle()
                            onStarProductsClick()
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
                            isUserActive = isUserActive,
                            onUnitAnchorCentersChanged = { projectedUnits = it },
                            onRotationAnimationFinished = onRotationAnimationFinished,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    if (vitrinaInteractionEnabled && showProducts) {
                        // Una sola capa para todos los perfiles: evita pelea cuerpo vs cintillo.
                        VitrinaActiveUnitTapLayer(
                            activeIndex = activeIndex,
                            activeUnitId = activeVitrinaUnit.unit.id,
                            projectedUnits = projectedUnits,
                            enabled = bubblesVisible && vitrinaInteractionEnabled,
                            tapEnabled = unitTapEnabled,
                            metrics = metrics,
                            dragSlopPx = dragPointerSlopPx,
                            onUnitClick = { unitId ->
                                onWakeFromIdle()
                                onUnitClick(unitId)
                            },
                            onDragStart = { onWakeFromIdle(); onDragStart() },
                            onDrag = onDrag,
                            onDragEnd = onDragEnd,
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

            // Manitos: siempre montadas (alpha) como Historia — no re-decodificar al volver.
            IntroActionButton(
                assetPath = VitrinaUiImages.GIRA_GIF,
                onClick = onRotateClick,
                size = metrics.rotateButtonSize,
                enabled = showOverlayControls,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = metrics.rotateButtonEndPadding)
                    .offset(
                        x = metrics.rotateButtonProtrudeOffset,
                        y = metrics.rotateButtonCenterYOffset,
                    )
                    .alpha(if (showOverlayControls) 1f else 0f)
                    .zIndex(40f),
            )

            val touchVisible =
                showOverlayControls && vitrinaInteractionEnabled && showProducts
            IntroActionButton(
                assetPath = VitrinaUiImages.TOUCH_GIF,
                onClick = {
                    onWakeFromIdle()
                    onUnitClick(activeVitrinaUnit.unit.id)
                },
                size = metrics.touchHintSize,
                enabled = touchVisible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = metrics.touchHintBottomPadding)
                    .offset(x = metrics.touchHintCenterXOffset)
                    .alpha(if (touchVisible) 1f else 0f)
                    .zIndex(40f),
            )
            // Historia: BadgeHistoria + gif en IntroLandscapeLayout (rail derecho).
        }
    }
}
