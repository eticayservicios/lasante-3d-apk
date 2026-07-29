package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation

/** Constantes compartidas de la vitrina — doc funcional §4 y §16. */
object VitrinaConstants {
    const val UNIT_COUNT = 5
    const val SLOTS_PER_UNIT = 4
    const val ROTATION_STEP_DEGREES = 72f
    /** Distancia horizontal mínima para snap ±1 unidad al soltar drag. */
    const val DRAG_SNAP_THRESHOLD_PX = 40f
    /** Grados de offset visual por píxel arrastrado (72° / 40px). */
    const val DRAG_DEGREES_PER_PX = ROTATION_STEP_DEGREES / DRAG_SNAP_THRESHOLD_PX

    /**
     * GLB vitrina en CloudFront (Draco, ~26 MB). Se cachea en disco al abrir Intro.
     * Asset local [BASE_GLB_ASSET] como fallback offline.
     */
    const val BASE_GLB_REMOTE =
        "https://d4cvafcfx7yef.cloudfront.net/vitrina/mobile_draco.glb"
    const val BASE_GLB_ASSET = "vitrina/models/mobile_draco.glb"
    /** @deprecated Usar [BASE_GLB_ASSET]. */
    const val BASE_GLB_FULL = BASE_GLB_ASSET

    private val snappyRotationEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /**
     * Bbox Filament del GLB mobile (incluye “palo” invisible hasta Y≈-72).
     * La vitrina visible ocupa solo ~3.26u (Y: -0.097 a 2.652).
     * MAX_EXTENT = bbox completo (scaleToUnits + gltfToSceneUnits).
     * VISIBLE_HEIGHT = altura visible para llenar el viewport.
     */
    const val BASE_GLB_MAX_EXTENT = 74.6f
    const val BASE_GLB_VISIBLE_HEIGHT = 3.26f
    const val BASE_GLB_TOP_Y = 2.652f
    const val BASE_GLB_VISIBLE_CENTER_Y = 1.28f
    /** Distancia desde el tope del bbox hasta el centro de la vitrina visible. */
    const val BASE_GLB_VISIBLE_CENTER_BELOW_TOP = BASE_GLB_TOP_Y - BASE_GLB_VISIBLE_CENTER_Y

    /** Export glTF Y-up; origen arriba del cilindro. */
    val baseModelRotation = Rotation()

    val FEATURED_ANCHOR_NAMES = listOf("featured_1", "featured_2", "featured_3", "featured_4")
    /** Nodos de unidad en mobile_draco.glb (orden API 0…4). */
    val UNIT_GLB_NODE_NAMES = listOf(
        "genericos_lasante",
        "primary_care",
        "specialty_care",
        "phq_consumo",
        "hospital_care",
    )
    /**
     * Empties radiales (export nuevo). Preferidos para tap/rotación porque
     * [UNIT_GLB_NODE_NAMES] suelen compartir la misma transform del cintillo.
     */
    val UNIT_SLOT_CATEGORIA_NAMES = listOf(
        "slot_categoria_1",
        "slot_categoria_2",
        "slot_categoria_3",
        "slot_categoria_4",
        "slot_categoria_5",
    )
    /** @deprecated Usar [UNIT_GLB_NODE_NAMES]. */
    val UNIT_ANCHOR_NAMES = UNIT_GLB_NODE_NAMES
    val LEGACY_ANCHOR_NAMES = listOf("slot_1", "slot_2", "slot_3", "slot_4")

    val UNIT_ACCENT_COLORS = listOf(
        Color(0xFF26A641),
        Color(0xFF2448D8),
        Color(0xFF7B2CBF),
        Color(0xFFFF9800),
        Color(0xFFE53935),
    )

    val fadeAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 280)
    val manualRotationAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = 180,
        easing = snappyRotationEasing,
    )
    val autoRotationAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 400)

    /** @deprecated Usar [manualRotationAnimationSpec] o [autoRotationAnimationSpec]. */
    val rotationAnimationSpec: AnimationSpec<Float> = manualRotationAnimationSpec

    /** Sube el ancla del slot a la superficie del tablón (coords glTF). */
    const val featuredProductShelfLiftGltf = 0.03f

    /** Desplaza la fila de destacados hacia la derecha (+X glTF). Ajustable sin reexportar el GLB. */
    const val featuredSlotsOffsetGltfX = 0.18f

    /** Centro del producto en coords glTF del slot (base del mesh sobre el tablón). */
    fun featuredProductCenterGltf(slotPosition: Position, scaleToUnits: Float, uniformScale: Float): Position {
        val halfHeightGltf = scaleToUnits / (2f * uniformScale)
        return Position(
            x = slotPosition.x,
            y = slotPosition.y + halfHeightGltf,
            z = slotPosition.z,
        )
    }

    /** Escala uniforme glTF→mundo: la franja visible (3.26u) llena el viewport. */
    fun visibleUniformScale(fillFraction: Float, viewportHeight: Float): Float =
        fillFraction * viewportHeight / BASE_GLB_VISIBLE_HEIGHT

    /**
     * Posición Y del nodo base: centra la vitrina visible en el origen (lookAtY≈0).
     * No usa scaleToUnits (bbox 74.6u) — escala manual con [visibleUniformScale].
     */
    fun baseModelPositionY(uniformScale: Float, posYOffset: Float = 0f): Float =
        -BASE_GLB_VISIBLE_CENTER_Y * uniformScale + posYOffset

    /** Escala de productos destacados ([scaleToUnits] en unidades de escena Filament). */
    fun featuredProductScale(gltfCoordScale: Float, ratio: Float): Float =
        gltfCoordScale * ratio

    /**
     * Factor legacy para slots/anchors: convierte coords glTF con la misma escala uniforme.
     * (Antes se derivaba de scaleToUnits sobre el bbox completo.)
     */
    fun gltfCoordScale(uniformScale: Float): Float =
        uniformScale * BASE_GLB_MAX_EXTENT / BASE_GLB_VISIBLE_HEIGHT

    /** Convierte coordenadas glTF a espacio local del nodo (con [visibleUniformScale]). */
    fun gltfToLocalUnits(gltfValue: Float, uniformScale: Float): Float =
        gltfValue * uniformScale

    /** @deprecated Usar [gltfToLocalUnits] para posiciones con escala uniforme. */
    fun gltfToSceneUnits(gltfValue: Float, gltfCoordScale: Float): Float =
        gltfValue / BASE_GLB_MAX_EXTENT * gltfCoordScale
}
