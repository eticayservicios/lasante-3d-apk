package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.abs

/**
 * Drag horizontal en los bordes laterales de la vitrina.
 */
fun Modifier.vitrinaEdgeDragGesture(
    dragSlopPx: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
): Modifier = vitrinaHorizontalDragGesture(
    dragSlopPx = dragSlopPx,
    onDragStart = onDragStart,
    onDrag = onDrag,
    onDragEnd = onDragEnd,
)

/**
 * Drag horizontal sobre toda el área de la vitrina (encima del SceneView).
 * Permite rotar deslizando el cilindro desde cualquier zona libre de tap.
 */
fun Modifier.vitrinaBodyDragGesture(
    dragSlopPx: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
): Modifier = vitrinaHorizontalDragGesture(
    dragSlopPx = dragSlopPx,
    onDragStart = onDragStart,
    onDrag = onDrag,
    onDragEnd = onDragEnd,
)

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
                if (abs(accumulatedX) >= dragSlopPx && abs(accumulatedX) > abs(delta.y) * 1.1f) {
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
