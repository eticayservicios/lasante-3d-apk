package com.lasante.tvkiosk.ui.screens.intro

import android.util.Log
import com.google.android.filament.Box
import io.github.sceneview.math.Position
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.model.engine
import io.github.sceneview.model.model

enum class VitrinaSlotSource {
    GLTF_ANCHOR,
    DESIGN_CONFIG,
}

data class VitrinaResolvedSlot(
    val position: Position,
    val rotationY: Float,
    val scale: Float,
    val source: VitrinaSlotSource,
)

data class VitrinaResolvedUnitAnchor(
    val index: Int,
    val name: String,
    val position: Position,
    val bearingDegrees: Float,
    val source: VitrinaSlotSource,
)

/**
 * Resuelve posiciones de producto desde empties del GLB base.
 *
 * Prioridad:
 * 1. `featured_1…featured_4`  (nuevo export ideal)
 * 2. configuración de diseño frontal  (`VitrinaSlotConfig`)
 *
 * No usamos `slot_1…slot_4` para destacados: esos anchors son radiales y
 * distribuyen productos alrededor de la corona, no como fila frontal.
 */
object VitrinaAnchorResolver {
    private const val TAG = "VitrinaAnchor"

    fun resolveFromInstance(
        instance: ModelInstance,
        legacySlots: List<VitrinaSlot>,
    ): List<VitrinaResolvedSlot> {
        val model = instance.model
        val transformManager = instance.engine.transformManager
        val foundNames = mutableListOf<String>()

        val featuredEntities = VitrinaConstants.FEATURED_ANCHOR_NAMES.map { name ->
            name to model.getFirstEntityByName(name)
        }
        val hasCompleteFeaturedSet = featuredEntities.all { (_, entity) -> entity != 0 }

        val resolved = legacySlots.mapIndexed { index, legacy ->
            val (matchedName, entity) = featuredEntities.getOrNull(index) ?: ("" to 0)

            if (hasCompleteFeaturedSet && entity != 0) {
                foundNames += matchedName
                val transformInstance = transformManager.getInstance(entity)
                val matrix = FloatArray(16)
                transformManager.getTransform(transformInstance, matrix)
                VitrinaResolvedSlot(
                    position = Position(
                        x = matrix[12],
                        y = matrix[13] + VitrinaConstants.featuredProductShelfLiftGltf,
                        z = matrix[14],
                    ),
                    rotationY = legacy.rotationY,
                    scale = legacy.scale,
                    source = VitrinaSlotSource.GLTF_ANCHOR,
                )
            } else {
                VitrinaResolvedSlot(
                    position = legacy.position,
                    rotationY = legacy.rotationY,
                    scale = legacy.scale,
                    source = VitrinaSlotSource.DESIGN_CONFIG,
                )
            }
        }

        logSummary(foundNames, resolved)
        return resolved
    }

    fun resolveUnitAnchorsFromInstance(instance: ModelInstance): List<VitrinaResolvedUnitAnchor> {
        // Preferir meshes del cintillo (altura + bearing correctos).
        // slot_categoria_* está abajo del cilindro (y≈0) y desalinea el hotspot.
        val fromMeshes = resolveUnitAnchorsByNames(
            instance = instance,
            names = VitrinaConstants.UNIT_GLB_NODE_NAMES,
            allowTransformFallback = false,
        )
        val resolved = if (fromMeshes.size == VitrinaConstants.UNIT_COUNT) {
            fromMeshes
        } else {
            resolveUnitAnchorsByNames(
                instance = instance,
                names = VitrinaConstants.UNIT_SLOT_CATEGORIA_NAMES,
                allowTransformFallback = true,
            )
        }

        val mode = when {
            fromMeshes.size == VitrinaConstants.UNIT_COUNT -> "UNIT_MESH"
            resolved.isNotEmpty() -> "SLOT_CATEGORIA"
            else -> "NONE"
        }
        Log.d(
            TAG,
            "units mode=$mode anchors=${resolved.size}/${VitrinaConstants.UNIT_COUNT} names=${resolved.map { it.name }}",
        )
        resolved.forEach { anchor ->
            Log.d(
                TAG,
                "unit ${anchor.index + 1} (${anchor.name}): bearing=${anchor.bearingDegrees.format()} " +
                    "pos=(${anchor.position.x.format()}, ${anchor.position.y.format()}, ${anchor.position.z.format()})",
            )
        }
        return resolved
    }

    private fun resolveUnitAnchorsByNames(
        instance: ModelInstance,
        names: List<String>,
        allowTransformFallback: Boolean,
    ): List<VitrinaResolvedUnitAnchor> {
        val model = instance.model
        val renderableManager = instance.engine.renderableManager
        val transformManager = instance.engine.transformManager

        return names.mapIndexedNotNull { index, name ->
            val entity = model.getFirstEntityByName(name)
            if (entity == 0) return@mapIndexedNotNull null

            val meshCenter = entityMeshCenter(instance, entity, renderableManager)
            val transformPos = entityTransformTranslation(entity, transformManager)
            // Cintillo visual ≈ Y -0.1 tras scale del nodo (no Y del mesh -65 ni Y nodo 1.46).
            val bandY = -0.1f
            val center = when {
                meshCenter != null -> {
                    val y = if (kotlin.math.abs(meshCenter.y) > 8f) {
                        bandY
                    } else {
                        meshCenter.y
                    }
                    Position(x = meshCenter.x, y = y, z = meshCenter.z)
                }
                allowTransformFallback && transformPos != null ->
                    Position(x = transformPos.x, y = bandY, z = transformPos.z)
                else -> null
            } ?: return@mapIndexedNotNull null

            val bearing = VitrinaRotation.bearingDegrees(center.x, center.z)
            VitrinaResolvedUnitAnchor(
                index = index,
                name = name,
                position = center,
                bearingDegrees = bearing,
                source = VitrinaSlotSource.GLTF_ANCHOR,
            )
        }
    }

    private fun entityMeshCenter(
        instance: ModelInstance,
        entity: Int,
        renderableManager: com.google.android.filament.RenderableManager,
    ): Position? {
        if (!renderableManager.hasComponent(entity)) return null

        val renderableInstance = renderableManager.getInstance(entity)
        val box = Box()
        renderableManager.getAxisAlignedBoundingBox(renderableInstance, box)
        val center = box.center
        return Position(center[0], center[1], center[2])
    }

    private fun entityTransformTranslation(
        entity: Int,
        transformManager: com.google.android.filament.TransformManager,
    ): Position? {
        if (!transformManager.hasComponent(entity)) return null
        val transformInstance = transformManager.getInstance(entity)
        val matrix = FloatArray(16)
        transformManager.getTransform(transformInstance, matrix)
        return Position(x = matrix[12], y = matrix[13], z = matrix[14])
    }

    private fun logSummary(foundNames: List<String>, resolved: List<VitrinaResolvedSlot>) {
        val anchorCount = foundNames.size
        val mode = when (anchorCount) {
            VitrinaConstants.SLOTS_PER_UNIT -> "GLTF"
            0 -> "DESIGN"
            else -> "MIXED"
        }
        Log.d(
            TAG,
            "mode=$mode anchors=$anchorCount/${VitrinaConstants.SLOTS_PER_UNIT} names=$foundNames design=${resolved.count { it.source == VitrinaSlotSource.DESIGN_CONFIG }}",
        )
        resolved.forEachIndexed { index, slot ->
            Log.d(
                TAG,
                "slot ${index + 1}: source=${slot.source} pos=(${slot.position.x.format()}, ${slot.position.y.format()}, ${slot.position.z.format()}) scaleToUnits=${slot.scale.format()}",
            )
        }
    }

    private fun Float.format(): String = "%.3f".format(this)
}
