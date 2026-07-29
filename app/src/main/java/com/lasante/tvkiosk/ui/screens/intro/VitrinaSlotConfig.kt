package com.lasante.tvkiosk.ui.screens.intro

import io.github.sceneview.math.Position
import kotlin.math.tan

data class VitrinaSlot(
    val position: Position,
    val scale: Float,
    val rotationY: Float = 0f,
)

data class VitrinaSceneMetrics(
    /** Escala uniforme del GLB base (franja visible → viewport). */
    val uniformScale: Float,
    /** Factor para coords glTF de slots / anchors. */
    val gltfCoordScale: Float,
    val cameraY: Float,
    val cameraZ: Float,
    val lookAtY: Float,
    val posYOffset: Float,
    val lightIntensity: Float,
    val featuredSlots: List<VitrinaSlot>,
)

object VitrinaSlotConfig {
    private const val FOV_DEG = 45.0

    fun sceneMetrics(layout: IntroLayoutMetrics): VitrinaSceneMetrics {
        val tier = layout.deviceTier

        val viewportHeight = 2f * tier.cameraZ * tan(Math.toRadians(FOV_DEG / 2)).toFloat()
        val uniformScale = VitrinaConstants.visibleUniformScale(tier.verticalFill, viewportHeight)
        val gltfCoordScale = VitrinaConstants.gltfCoordScale(uniformScale)
        // scaleToUnits = unidades de escena (SceneView no re-escala por el uniformScale del padre).
        val featuredScale =
            VitrinaConstants.featuredProductScale(gltfCoordScale, tier.featuredScaleRatio)
        val featuredSlots = samuelSlotPositions(featuredScale)

        return VitrinaSceneMetrics(
            uniformScale = uniformScale,
            gltfCoordScale = gltfCoordScale,
            cameraY = tier.cameraY,
            cameraZ = tier.cameraZ,
            lookAtY = tier.lookAtY,
            posYOffset = tier.posYOffset,
            lightIntensity = if (layout.isTv) 340_000f else 230_000f,
            featuredSlots = featuredSlots,
        )
    }

    private fun samuelSlotPositions(featuredScale: Float): List<VitrinaSlot> {
        // Coordenadas de diseño: una fila frontal, metida hacia el centro de la corona superior.
        // No usamos los slot_1..slot_4 radiales del GLB porque reparten los productos alrededor.
        val frontalShelf = listOf(
            Triple(-0.96f, 1.405f, 0.78f),
            Triple(-0.32f, 1.405f, 0.86f),
            Triple(0.32f, 1.405f, 0.86f),
            Triple(0.96f, 1.405f, 0.78f),
        )
        return frontalShelf.map { gltf ->
            VitrinaSlot(
                position = Position(
                    gltf.first,
                    gltf.second + VitrinaConstants.featuredProductShelfLiftGltf,
                    gltf.third,
                ),
                scale = featuredScale,
                rotationY = 0f,
            )
        }
    }

    /** Presets calibrados — tuner 2026-07-06 (GLB mobile). Cámara siempre frontal. */
    private val IntroLayoutMetrics.deviceTier: DeviceTier
        get() = when (vitrinaProfileKey) {
            "tv_42" -> demoTier(
                verticalFill = 0.96f,
                cameraZ = 2.38f,
                cameraY = 0f,
                lookAtY = 0f,
                posYOffset = 0.26f,
            )
            "tv_32", "tv_66" -> demoTier(
                verticalFill = 0.96f,
                cameraZ = 2.40f,
                cameraY = 0f,
                lookAtY = 0f,
                posYOffset = 0.28f,
            )
            "phone_landscape" -> demoTier(
                // Cilindro intacto. Solo bajar burbujas vía bubblesRowTopInScene.
                verticalFill = 0.93f,
                cameraZ = 2.15f,
                cameraY = 0f,
                lookAtY = 0f,
                posYOffset = 0.26f,
                featuredScaleRatio = 0.021f,
            )
            "tablet_landscape" -> demoTier(
                // Mismo encuadre que tv_42 (tablet del proyecto unificado con TV 42).
                verticalFill = 0.96f,
                cameraZ = 2.38f,
                cameraY = 0f,
                lookAtY = 0f,
                posYOffset = 0.26f,
            )
            "phone_portrait" -> demoTier(verticalFill = 0.60f, cameraZ = 2.80f, cameraY = 0.0f, lookAtY = 0.0f)
            "short_height" -> demoTier(verticalFill = 0.91f, cameraZ = 2.10f, cameraY = 0.0f, lookAtY = 0.0f)
            "expanded" -> demoTier(verticalFill = 0.93f, cameraZ = 2.10f, cameraY = 0.0f, lookAtY = 0.0f)
            else -> demoTier()
        }

    private fun demoTier(
        verticalFill: Float = 0.90f,
        cameraZ: Float = 2.10f,
        cameraY: Float = 0.0f,
        lookAtY: Float = 0.0f,
        posYOffset: Float = 0f,
        featuredScaleRatio: Float = 0.020f,
    ) = DeviceTier(
        verticalFill = verticalFill,
        featuredScaleRatio = featuredScaleRatio,
        cameraZ = cameraZ,
        cameraY = cameraY,
        lookAtY = lookAtY,
        posYOffset = posYOffset,
    )

    private data class DeviceTier(
        val verticalFill: Float,
        val featuredScaleRatio: Float,
        val cameraZ: Float,
        val cameraY: Float,
        val lookAtY: Float,
        val posYOffset: Float,
    )
}
