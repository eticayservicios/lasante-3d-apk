package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import io.github.sceneview.math.Rotation

/** Constantes compartidas de la vitrina — doc funcional §4 y §16. */
object VitrinaConstants {
    const val UNIT_COUNT = 5
    const val SLOTS_PER_UNIT = 4
    const val ROTATION_STEP_DEGREES = 72f
    /**
     * Fracción del ancho de pantalla ≈ 1 unidad (72°) mientras arrastras.
     * Menor = más sensible (sigue mejor el dedo).
     * Baseline phone; pantallas grandes usan [dragTrackWidthScreenFraction].
     */
    const val DRAG_TRACK_WIDTH_SCREEN_FRACTION = 0.18f
    /**
     * Fracción de un paso (72°) para comprometer snap al soltar.
     * Por debajo vuelve a la unidad actual; al superarla avanza al menos ±1 unidad.
     */
    const val DRAG_SNAP_COMMIT_FRACTION = 0.10f
    /** Flick rápido (px/ms) que compromete ±1 aunque el arrastre sea corto. */
    const val DRAG_FLICK_VELOCITY_PX_PER_MS = 1.0f

    /**
     * Sensibilidad de arrastre por perfil. En TV/tablet el mismo % de pantalla
     * es un swipe físico más largo → se reduce la fracción para que el dedo responda.
     */
    fun dragTrackWidthScreenFraction(profileKey: String): Float = when (profileKey) {
        "phone_landscape", "phone_portrait" -> DRAG_TRACK_WIDTH_SCREEN_FRACTION
        "tv_32" -> 0.12f
        "tv_42", "tablet_landscape" -> 0.10f
        "tv_66", "expanded" -> 0.08f
        else -> 0.12f
    }

    /** Asset local del GLB de vitrina (Draco). */
    const val BASE_GLB_ASSET = "vitrina/models/mobile_draco.glb"

    /**
     * IBL embebido en SceneView (neutral). Se usa solo como IndirectLight;
     * nunca como skybox (el fondo de la app permanece claro).
     */
    const val STUDIO_IBL_KTX_ASSET = "environments/neutral/neutral_ibl.ktx"
    /**
     * Intensidad del IndirectLight. Default Filament ≈ 30000 (muy claro).
     * Más bajo ≈ estudio oscuro: reflejos menos lavados sobre el acrílico.
     */
    const val STUDIO_IBL_INTENSITY = 10_000f

    /**
     * Baseline de rotación idle (phone). Tablet/TV usan [IntroLayoutMetrics.idleFullRotationMs]
     * porque el mismo °/s se percibe más rápido en pantallas grandes.
     */
    const val IDLE_FULL_ROTATION_MS = 60_000

    private val snappyRotationEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /**
     * Bbox Filament del GLB mobile (incluye “palo” invisible hasta Y≈-72).
     * La vitrina visible ocupa solo ~3.26u (Y: -0.097 a 2.652).
     * MAX_EXTENT = bbox completo; VISIBLE_HEIGHT = altura visible para llenar el viewport.
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
    /**
     * Nodos GLB en orden angular (~72°), igual que API/admin:
     * Genéricos → Primary → Specialty → PHQ → Hospital.
     * Nombre de nodo = texto del cintillo = ID.
     *
     * Mantener sincronizado con [VitrinaGlbMapping.faces].
     */
    val UNIT_GLB_NODE_NAMES = VitrinaGlbMapping.orderedGlbNodeNames
    /**
     * Bearing XZ (grados) del centro de cada cintillo.
     * Misma orden que [UNIT_GLB_NODE_NAMES] / [VitrinaGlbMapping.faces].
     */
    val UNIT_MESH_BEARING_DEGREES = VitrinaGlbMapping.faces.map { it.bearingDegrees }
    /**
     * Empties radiales en el mismo orden angular que [UNIT_GLB_NODE_NAMES].
     */
    val UNIT_SLOT_CATEGORIA_NAMES = listOf(
        "slot_categoria_1",
        "slot_categoria_2",
        "slot_categoria_3", // Specialty
        "slot_categoria_4", // PHQ
        "slot_categoria_5",
    )

    /**
     * Lámparas del GLB (`shade.*`): 2 por cara, estante alto + bajo.
     * Ambas deben encender juntas con la cara activa.
     *
     * Índice = cara GLB: 0 Genéricos … 4 Hospital.
     */
    val LAMP_NODES_BY_FACE: List<List<String>> = listOf(
        listOf("shade.003", "shade.008"), // genericos (~91°)
        listOf("shade.002", "shade.006"), // primary (~163°)
        listOf("shade.001", "shade.007"), // specialty (~235°)
        listOf("shade.005", "shade.010"), // phq (~307°)
        listOf("shade.004", "shade.009"), // hospital (~19°)
    )
    val LAMP_NODE_NAMES: List<String> = LAMP_NODES_BY_FACE.flatten()
    /** Slots de material en cada mesh shade: bake / LED / Luz Interior. */
    const val LAMP_PRIMITIVE_BAKE = 0
    const val LAMP_PRIMITIVE_LED = 1
    const val LAMP_PRIMITIVE_INTERIOR = 2
    const val LAMP_BAKE_MATERIAL = "mtl_busselamp_bake"
    const val LAMP_LED_MATERIAL = "mtl_busselamp_led"
    const val LAMP_INTERIOR_MATERIAL = "Luz Interior"
    /** Valores ON del export Blender (apagado = 0). */
    const val LAMP_LED_EMISSIVE_STRENGTH = 10f
    const val LAMP_INTERIOR_EMISSIVE_STRENGTH = 5f
    val LAMP_LED_EMISSIVE_FACTOR = floatArrayOf(1f, 0.87941f, 0.52251f)
    val LAMP_INTERIOR_EMISSIVE_FACTOR = floatArrayOf(1f, 1f, 1f)

    val fadeAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 280)
    val manualRotationAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = 180,
        easing = snappyRotationEasing,
    )
    /** Snap al soltar drag (más corto que rotación manual genérica). */
    val dragSnapAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = 120,
        easing = snappyRotationEasing,
    )
    val autoRotationAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = 4000,
        easing = LinearEasing,
    )

    /** Sube el ancla del slot a la superficie del tablón (coords glTF). */
    const val featuredProductShelfLiftGltf = 0.03f

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
}
