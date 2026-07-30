package com.lasante.tvkiosk.ui.screens.intro

import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode

/**
 * Aplica rotación Y del cilindro base directamente en Filament, sin pasar por recomposición
 * del [ModelNode] composable en cada frame de drag o animación.
 */
class VitrinaBaseRotationHandle {
    private var node: ModelNode? = null
    private var baseDegrees: Float = 0f
    private var dragOffsetDegrees: Float = 0f

    fun bind(node: ModelNode) {
        this.node = node
        apply()
    }

    fun setBaseDegrees(degrees: Float) {
        baseDegrees = degrees
        apply()
    }

    fun setDragOffset(degrees: Float) {
        dragOffsetDegrees = degrees
        apply()
    }

    fun clearDragOffset() {
        dragOffsetDegrees = 0f
        apply()
    }

    /** Incorpora el offset de drag en la base (sin salto visual al soltar). */
    fun commitDragIntoBase() {
        baseDegrees = VitrinaRotation.normalizeDegrees(baseDegrees + dragOffsetDegrees)
        dragOffsetDegrees = 0f
        apply()
    }

    /** Ángulo Y aplicado ahora mismo (base + offset de drag). */
    fun currentDegrees(): Float =
        VitrinaRotation.normalizeDegrees(baseDegrees + dragOffsetDegrees)

    /** Reaplica tras SideEffect del composable SceneView que resetea rotation al valor fijo. */
    fun reapply() {
        apply()
    }

    private fun apply() {
        val target = node ?: return
        val angle = VitrinaRotation.normalizeDegrees(baseDegrees + dragOffsetDegrees)
        target.rotation = Rotation(
            x = VitrinaConstants.baseModelRotation.x,
            y = VitrinaConstants.baseModelRotation.y + angle,
            z = VitrinaConstants.baseModelRotation.z,
        )
    }
}
