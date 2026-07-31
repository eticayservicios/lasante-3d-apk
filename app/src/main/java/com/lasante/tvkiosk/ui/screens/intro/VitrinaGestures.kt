package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.abs

/** Drag horizontal en los bordes laterales. */
fun Modifier.vitrinaEdgeDragGesture(
    dragSlopPx: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
): Modifier = vitrinaHorizontalDragGesture(dragSlopPx, onDragStart, onDrag, onDragEnd)

/**
 * Capa única sobre la vitrina (todos los perfiles):
 * swipe horizontal → rotar; tap corto → [onTap] con la posición del toque.
 *
 * Usa [rememberUpdatedState] para que el tap use siempre el [activeIndex] /
 * unitId actuales (si no, tras girar el pointerInput queda con la unidad anterior).
 */
fun Modifier.vitrinaTapOrHorizontalDragGesture(
    dragSlopPx: Float,
    onTap: (Offset) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
): Modifier = composed {
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    pointerInput(dragSlopPx) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var dragging = false
            var accumulatedX = 0f
            var accumulatedY = 0f

            try {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id }
                    if (change == null) break

                    if (!change.pressed) {
                        if (dragging) {
                            currentOnDragEnd()
                            dragging = false
                        } else if (abs(accumulatedX) < dragSlopPx && abs(accumulatedY) < dragSlopPx) {
                            currentOnTap(down.position)
                        }
                        break
                    }

                    val delta = change.positionChange()
                    if (!dragging) {
                        accumulatedX += delta.x
                        accumulatedY += delta.y
                        if (abs(accumulatedX) >= dragSlopPx &&
                            abs(accumulatedX) >= abs(accumulatedY)
                        ) {
                            dragging = true
                            change.consume()
                            currentOnDragStart()
                            currentOnDrag(accumulatedX)
                        }
                    } else {
                        change.consume()
                        currentOnDrag(delta.x)
                    }
                }
            } finally {
                // Cancelación / pointer perdido / composición: no dejar modo Dragging colgado.
                if (dragging) currentOnDragEnd()
            }
        }
    }
}

private fun Modifier.vitrinaHorizontalDragGesture(
    dragSlopPx: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
): Modifier = composed {
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    pointerInput(dragSlopPx) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var dragging = false
            var accumulatedX = 0f
            var accumulatedY = 0f

            try {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id }
                    if (change == null) break

                    if (!change.pressed) {
                        if (dragging) {
                            currentOnDragEnd()
                            dragging = false
                        }
                        break
                    }

                    val delta = change.positionChange()
                    if (!dragging) {
                        accumulatedX += delta.x
                        accumulatedY += delta.y
                        if (abs(accumulatedX) >= dragSlopPx &&
                            abs(accumulatedX) >= abs(accumulatedY)
                        ) {
                            dragging = true
                            change.consume()
                            currentOnDragStart()
                            currentOnDrag(accumulatedX)
                        }
                    } else {
                        change.consume()
                        currentOnDrag(delta.x)
                    }
                }
            } finally {
                if (dragging) currentOnDragEnd()
            }
        }
    }
}
