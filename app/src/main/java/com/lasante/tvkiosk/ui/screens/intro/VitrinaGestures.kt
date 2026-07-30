package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
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
 */
fun Modifier.vitrinaTapOrHorizontalDragGesture(
    dragSlopPx: Float,
    onTap: (Offset) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
): Modifier = pointerInput(dragSlopPx) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var dragging = false
        var accumulatedX = 0f
        var accumulatedY = 0f

        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break

            if (!change.pressed) {
                if (dragging) {
                    onDragEnd()
                } else if (abs(accumulatedX) < dragSlopPx && abs(accumulatedY) < dragSlopPx) {
                    onTap(down.position)
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
                    onDragStart()
                    onDrag(accumulatedX)
                }
            } else {
                change.consume()
                onDrag(delta.x)
            }
        }
    }
}

private fun Modifier.vitrinaHorizontalDragGesture(
    dragSlopPx: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
): Modifier = pointerInput(dragSlopPx) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var dragging = false
        var accumulatedX = 0f
        var accumulatedY = 0f

        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break

            if (!change.pressed) {
                if (dragging) onDragEnd()
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
                    onDragStart()
                    onDrag(accumulatedX)
                }
            } else {
                change.consume()
                onDrag(delta.x)
            }
        }
    }
}
