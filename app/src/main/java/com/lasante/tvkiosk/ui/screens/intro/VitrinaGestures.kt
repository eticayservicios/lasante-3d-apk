package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.abs

/**
 * Drag horizontal en bordes o cuerpo de la vitrina (misma lógica).
 * Los wrappers públicos mantienen el call-site legible (edge vs body).
 */
fun Modifier.vitrinaEdgeDragGesture(
    dragSlopPx: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
): Modifier = vitrinaHorizontalDragGesture(dragSlopPx, onDragStart, onDrag, onDragEnd)

fun Modifier.vitrinaBodyDragGesture(
    dragSlopPx: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
): Modifier = vitrinaHorizontalDragGesture(dragSlopPx, onDragStart, onDrag, onDragEnd)

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
                    abs(accumulatedX) > abs(accumulatedY) * 1.05f
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
