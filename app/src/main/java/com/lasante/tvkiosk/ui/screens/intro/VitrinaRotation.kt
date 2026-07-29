package com.lasante.tvkiosk.ui.screens.intro

import kotlin.math.atan2

/**
 * Rotación Y del cilindro para dejar la unidad activa **de frente** a la cámara (+Z).
 *
 * Fórmula: `rotationY = MODEL_FRONT_OFFSET − bearing(centro radial de la unidad)`.
 * Los productos destacados no usan esta rotación (permanecen fijos al frente).
 */
object VitrinaRotation {
    /** Ajuste global mesh ↔ cámara. Calibrar si alguna unidad sigue de medio lado. */
    const val MODEL_FRONT_OFFSET_DEGREES = 0f

    fun bearingDegrees(x: Float, z: Float): Float =
        Math.toDegrees(atan2(x.toDouble(), z.toDouble())).toFloat()

    fun modelRotationYForBearing(bearingDegrees: Float): Float =
        normalizeDegrees(MODEL_FRONT_OFFSET_DEGREES - bearingDegrees)

    fun rotationForGlbIndex(
        glbIndex: Int,
        anchors: List<VitrinaResolvedUnitAnchor>,
    ): Float {
        val count = VitrinaConstants.UNIT_COUNT
        if (count <= 0) return 0f
        val safeIndex = ((glbIndex % count) + count) % count

        val anchor = anchors.find { it.index == safeIndex }
        val bearing = anchor?.bearingDegrees
            ?: (safeIndex * VitrinaConstants.ROTATION_STEP_DEGREES)
        return modelRotationYForBearing(bearing)
    }

    fun rotationForActiveUnit(
        apiIndex: Int,
        anchors: List<VitrinaResolvedUnitAnchor>,
    ): Float = rotationForGlbIndex(VitrinaGlbMapping.glbIndexFor(apiIndex), anchors)

    /** Elige un ángulo equivalente cercano a [current] para animar por el arco corto. */
    fun nearestEquivalentAngle(current: Float, target: Float): Float {
        val normalizedTarget = normalizeDegrees(target)
        var candidate = normalizedTarget
        while (candidate - current > 180f) candidate -= 360f
        while (candidate - current < -180f) candidate += 360f
        return candidate
    }

    fun normalizeDegrees(degrees: Float): Float =
        ((degrees % 360f) + 360f) % 360f
}
