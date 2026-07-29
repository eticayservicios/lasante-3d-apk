package com.lasante.tvkiosk.ui.screens.intro

import io.github.sceneview.math.Position
import kotlin.math.tan

/** Proyección manual glTF → fracciones de pantalla (0–1) en el área 3D. */
object VitrinaScreenProjection {

    fun projectPoint(
        slotGltf: Position,
        scene: VitrinaSceneMetrics,
        baseY: Float,
        rotationDegrees: Float,
        aspectRatio: Float,
    ): FeaturedSlotScreenPoint? {
        val world = featuredSlotWorldPosition(
            slotGltf = slotGltf,
            rotationDegrees = rotationDegrees,
            uniformScale = scene.uniformScale,
            baseY = baseY,
        ).toVec3()

        val camera = Vec3(0f, scene.cameraY, scene.cameraZ)
        val target = Vec3(0f, scene.lookAtY, 0f)
        val forward = (target - camera).normalized()
        val worldUp = Vec3(0f, 1f, 0f)
        val right = forward.cross(worldUp).normalized()
        val up = right.cross(forward).normalized()
        val tanHalfFov = tan(Math.toRadians(45.0 / 2.0)).toFloat()

        val toPoint = world - camera
        val depth = toPoint.dot(forward)
        if (depth <= 0.01f) return null

        val viewX = toPoint.dot(right)
        val viewY = toPoint.dot(up)
        val ndcX = viewX / (depth * tanHalfFov * aspectRatio)
        val ndcY = viewY / (depth * tanHalfFov)
        val screenX = (0.5f + ndcX * 0.5f).coerceIn(0f, 1f)
        val screenY = (0.5f - ndcY * 0.5f).coerceIn(0f, 1f)
        return FeaturedSlotScreenPoint(screenX, screenY)
    }

    fun projectSlots(
        slots: List<VitrinaResolvedSlot>,
        scene: VitrinaSceneMetrics,
        baseY: Float,
        rotationDegrees: Float,
        aspectRatio: Float,
    ): List<FeaturedSlotScreenPoint> =
        slots.mapNotNull { slot ->
            projectPoint(
                slotGltf = slot.position,
                scene = scene,
                baseY = baseY,
                rotationDegrees = rotationDegrees,
                aspectRatio = aspectRatio,
            )
        }

    private fun featuredSlotWorldPosition(
        slotGltf: Position,
        rotationDegrees: Float,
        uniformScale: Float,
        baseY: Float,
    ): Position {
        val angleRad = Math.toRadians(rotationDegrees.toDouble())
        val cosA = kotlin.math.cos(angleRad).toFloat()
        val sinA = kotlin.math.sin(angleRad).toFloat()
        val x = slotGltf.x * uniformScale
        val z = slotGltf.z * uniformScale
        return Position(
            x = cosA * x + sinA * z,
            y = slotGltf.y * uniformScale + baseY,
            z = -sinA * x + cosA * z,
        )
    }

    private data class Vec3(val x: Float, val y: Float, val z: Float) {
        operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
        fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z
        fun cross(other: Vec3): Vec3 = Vec3(
            x = y * other.z - z * other.y,
            y = z * other.x - x * other.z,
            z = x * other.y - y * other.x,
        )
        fun normalized(): Vec3 {
            val length = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat().coerceAtLeast(1e-6f)
            return Vec3(x / length, y / length, z / length)
        }
    }

    private fun Position.toVec3() = Vec3(x, y, z)
}
