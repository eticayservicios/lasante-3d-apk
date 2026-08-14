package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import com.lasante.tvkiosk.ui.utils.modalBackdropBlur
import com.google.android.filament.MaterialInstance
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.snapshotFlow
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.environment.Environment
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.model.model
import io.github.sceneview.node.ModelNode
import com.google.android.filament.RenderableManager
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.safeDestroySkybox
import kotlin.math.abs

private data class VitrinaSwapMaterials(
    val matOn: MaterialInstance,
    val matOff: MaterialInstance,
    val glassOn: MaterialInstance,
    val glassOff: MaterialInstance,
    /**
     * LED / Luz Interior ON+OFF.
     * El GLB comparte una sola instancia entre las 10 lámparas; hay que clonar OFF
     * para poder apagar laterales y dejar solo la cara frontal encendida.
     */
    val lampLedOn: MaterialInstance?,
    val lampLedOff: MaterialInstance?,
    val lampInteriorOn: MaterialInstance?,
    val lampInteriorOff: MaterialInstance?,
)

private fun duplicateLampOff(
    source: MaterialInstance?,
    offName: String,
): MaterialInstance? {
    source ?: return null
    return try {
        MaterialInstance.duplicate(source, offName).also { off ->
            // Filament puede no exponer estos params; nunca tumbar Intro.
            runCatching { off.setParameter("emissiveFactor", 0f, 0f, 0f) }
            runCatching { off.setParameter("emissiveStrength", 0f) }
        }
    } catch (t: Throwable) {
        VitrinaDebugLog.w("VitrinaDiag", "duplicateLampOff($offName) failed: ${t.message}")
        null
    }
}

private fun applyFaceLamps(
    instance: ModelInstance,
    renderableManager: RenderableManager,
    mats: VitrinaSwapMaterials,
    litFaceIndex: Int,
) {
    val ledOn = mats.lampLedOn ?: return
    val ledOff = mats.lampLedOff ?: return
    val intOn = mats.lampInteriorOn
    val intOff = mats.lampInteriorOff

    VitrinaConstants.LAMP_NODES_BY_FACE.forEachIndexed { faceIndex, nodeNames ->
        val on = faceIndex == litFaceIndex
        val led = if (on) ledOn else ledOff
        val interior = when {
            intOn == null || intOff == null -> null
            on -> intOn
            else -> intOff
        }
        nodeNames.forEach { nodeName ->
            try {
                val entity = instance.model.getFirstEntityByName(nodeName)
                if (entity == 0 || !renderableManager.hasComponent(entity)) return@forEach
                val ri = renderableManager.getInstance(entity)
                if (ri == 0) return@forEach
                val primCount = renderableManager.getPrimitiveCount(ri)
                val ledSlot = VitrinaConstants.LAMP_PRIMITIVE_LED
                val intSlot = VitrinaConstants.LAMP_PRIMITIVE_INTERIOR
                // Evitar abort nativo Filament si el mesh no tiene 3 primitives.
                if (ledSlot < primCount) {
                    renderableManager.setMaterialInstanceAt(ri, ledSlot, led)
                }
                if (interior != null && intSlot < primCount) {
                    renderableManager.setMaterialInstanceAt(ri, intSlot, interior)
                }
            } catch (t: Throwable) {
                VitrinaDebugLog.w(
                    "VitrinaDiag",
                    "lamp swap $nodeName failed: ${t.message}",
                )
            }
        }
    }
}

@Composable
fun VitrinaModelViewer(
    activeUnitId: String,
    activeIndex: Int,
    modifier: Modifier = Modifier,
    displayRotationDegrees: Float,
    baseRotationHandle: VitrinaBaseRotationHandle,
    filamentSession: VitrinaFilamentSession,
    rotationAnimationSpec: AnimationSpec<Float> = VitrinaConstants.manualRotationAnimationSpec,
    layoutMetrics: IntroLayoutMetrics,
    sceneActive: Boolean = true,
    filamentRenderingEnabled: Boolean = true,
    backdropBlurred: Boolean = false,
    isDragging: Boolean = false,
    isUserActive: Boolean = true,
    onUnitAnchorCentersChanged: (Map<Int, FeaturedSlotScreenPoint>) -> Unit = {},
    onRotationAnimationFinished: () -> Unit = {},
) {
    val appContext = LocalContext.current.applicationContext
    val scene = VitrinaSlotConfig.sceneMetrics(layoutMetrics)
    val renderQuality = remember(layoutMetrics.deviceProfile) {
        VitrinaDeviceLoadPolicy.filamentRenderQuality(appContext, layoutMetrics)
    }

    val engine = filamentSession.engine
    val modelLoader = filamentSession.modelLoader
    val sceneLifecycle = rememberVitrinaSceneLifecycle(renderingEnabled = filamentRenderingEnabled)
    val environmentLoader = rememberEnvironmentLoader(engine)
    // IBL de estudio oscuro (KTX neutral de SceneView) SIN skybox:
    // - El usuario sigue viendo el fondo claro de Compose (SceneView isOpaque=false).
    // - Los reflejos/irradiance del acrílico usan un entorno más oscuro → look lechoso.
    val environment = rememberEnvironment(environmentLoader, isOpaque = false) {
        val env = environmentLoader.createKTX1Environment(
            iblAssetFile = VitrinaConstants.STUDIO_IBL_KTX_ASSET,
            skyboxAssetFile = null,
        )
        env.skybox?.let { engine.safeDestroySkybox(it) }
        env.indirectLight?.intensity = VitrinaConstants.STUDIO_IBL_INTENSITY
        Environment(
            indirectLight = env.indirectLight,
            skybox = null,
            sphericalHarmonics = env.sphericalHarmonics,
        )
    }
    val baseInstance = filamentSession.baseInstance

    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = scene.cameraY, z = scene.cameraZ)
        lookAt(Position(x = 0f, y = scene.lookAtY, z = 0f), smooth = false)
    }
    LaunchedEffect(scene.cameraY, scene.cameraZ, scene.lookAtY) {
        cameraNode.position = Position(x = 0f, y = scene.cameraY, z = scene.cameraZ)
        cameraNode.lookAt(Position(x = 0f, y = scene.lookAtY, z = 0f), smooth = false)
    }

    // Luz principal = default SceneView (sin color/dirección/intensidad custom).
    val mainLightNode = rememberMainLightNode(engine)

    val unitAnchors = remember(baseInstance) {
        baseInstance?.let { instance ->
            VitrinaAnchorResolver.resolveUnitAnchorsFromInstance(instance)
        }.orEmpty()
    }

    val targetRotationDegrees = remember(activeIndex, unitAnchors, displayRotationDegrees) {
        if (unitAnchors.isNotEmpty()) {
            VitrinaRotation.rotationForActiveUnit(activeIndex, unitAnchors)
        } else {
            displayRotationDegrees
        }
    }
    val rotationAnim = remember { Animatable(displayRotationDegrees) }
    LaunchedEffect(Unit) {
        baseRotationHandle.setBaseDegrees(displayRotationDegrees)
        rotationAnim.snapTo(displayRotationDegrees)
    }
    LaunchedEffect(
        targetRotationDegrees,
        rotationAnimationSpec,
        sceneActive,
        isDragging,
        isUserActive,
        layoutMetrics.idleFullRotationMs,
    ) {
        if (isDragging) return@LaunchedEffect
        if (!sceneActive) {
            rotationAnim.snapTo(targetRotationDegrees)
            baseRotationHandle.setBaseDegrees(targetRotationDegrees)
            return@LaunchedEffect
        }
        if (!isUserActive) {
            // Rotación continua idle. Duración por perfil: pantallas grandes
            // usan más ms/360° porque el mismo °/s se percibe más rápido (más px en el borde).
            val startAngle = rotationAnim.value
            rotationAnim.animateTo(
                targetValue = startAngle - 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = layoutMetrics.idleFullRotationMs,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Restart
                )
            ) {
                baseRotationHandle.setBaseDegrees(value)
            }
        } else {
            val visual = VitrinaRotation.nearestEquivalentAngle(
                rotationAnim.value,
                baseRotationHandle.currentDegrees(),
            )
            if (kotlin.math.abs(rotationAnim.value - visual) > 0.5f) {
                rotationAnim.snapTo(visual)
            }
            val target = VitrinaRotation.nearestEquivalentAngle(rotationAnim.value, targetRotationDegrees)
            if (kotlin.math.abs(rotationAnim.value - target) < 0.05f) {
                rotationAnim.snapTo(target)
                baseRotationHandle.setBaseDegrees(target)
            } else {
                rotationAnim.animateTo(target, rotationAnimationSpec) {
                    baseRotationHandle.setBaseDegrees(value)
                }
            }
            onRotationAnimationFinished()
        }
    }

    LaunchedEffect(activeUnitId, activeIndex, targetRotationDegrees) {
        VitrinaDebugLog.d(
            "VitrinaRotation",
            "unit=$activeUnitId index=$activeIndex glb=${VitrinaGlbMapping.glbNodeNameFor(activeIndex)} " +
                "faceTarget=$targetRotationDegrees appliedY=${VitrinaConstants.baseModelRotation.y + targetRotationDegrees}",
        )
    }

    val baseY = VitrinaConstants.baseModelPositionY(scene.uniformScale, scene.posYOffset)
    val staticBaseRotation = VitrinaConstants.baseModelRotation

    LaunchedEffect(
        sceneActive,
        activeUnitId,
        unitAnchors,
        targetRotationDegrees,
        scene.cameraY,
        scene.cameraZ,
        scene.lookAtY,
        scene.uniformScale,
        baseY,
        layoutMetrics.maxWidth,
        layoutMetrics.maxHeight,
        layoutMetrics.sceneWidthFraction,
        layoutMetrics.sceneHeightFraction,
    ) {
        if (!sceneActive) return@LaunchedEffect
        val projectionRotation = targetRotationDegrees
        val sceneHeight = layoutMetrics.maxHeight.value * layoutMetrics.sceneHeightFraction
        val sceneWidth = layoutMetrics.maxWidth.value * layoutMetrics.sceneWidthFraction
        val aspectRatio = if (sceneHeight > 0f) sceneWidth / sceneHeight else 1f
        val unitCenters = projectUnitAnchorCenters(
            anchors = unitAnchors,
            scene = scene,
            baseY = baseY,
            rotationAngle = VitrinaConstants.baseModelRotation.y + projectionRotation,
            aspectRatio = aspectRatio,
        )
        VitrinaDebugLog.d(
            "VitrinaAnchor",
            "unit projections=${unitCenters.size}/${unitAnchors.size} activeUnit=$activeUnitId rotation=${VitrinaConstants.baseModelRotation.y + projectionRotation}",
        )
        onUnitAnchorCentersChanged(unitCenters)
        VitrinaUnitBandDiagnostics.logScreenProjections(
            activeGlbIndex = VitrinaGlbMapping.glbIndexFor(activeIndex),
            projectedUnits = unitCenters,
            rotationDegrees = VitrinaConstants.baseModelRotation.y + projectionRotation,
        )
    }

    // Intercambio dinámico de materiales para el cintillo y el acrílico.
    // Filament solo instancia materiales referenciados por ≥1 mesh del GLB.
    var swapMaterials by remember(baseInstance) { mutableStateOf<VitrinaSwapMaterials?>(null) }

    LaunchedEffect(baseInstance) {
        val instance = baseInstance ?: run {
            swapMaterials = null
            return@LaunchedEffect
        }
        var matOn: MaterialInstance? = null
        var matOff: MaterialInstance? = null
        var glassOn: MaterialInstance? = null
        var glassOff: MaterialInstance? = null
        var lampLed: MaterialInstance? = null
        var lampInterior: MaterialInstance? = null
        val materialInstances = instance.getMaterialInstances()
        materialInstances.forEachIndexed { idx, mat ->
            val name = try { mat.getName() ?: "" } catch (_: Exception) { "" }
            VitrinaDebugLog.d("VitrinaMaterials", "MaterialInstance[$idx]: name='$name'")
            when (name) {
                "MAT_SECCION_ON" -> matOn = mat
                "MAT_SECCION_OFF" -> matOff = mat
                "GLASS.on" -> glassOn = mat
                "GLASS" -> glassOff = mat
                VitrinaConstants.LAMP_LED_MATERIAL -> lampLed = mat
                VitrinaConstants.LAMP_INTERIOR_MATERIAL -> lampInterior = mat
            }
        }
        if (matOn == null || matOff == null || glassOn == null || glassOff == null) {
            VitrinaDebugLog.e(
                "VitrinaMaterials",
                "Materiales incompletos (¿OFF sin mesh en GLB?). " +
                    "matOn=$matOn matOff=$matOff glassOn=$glassOn glassOff=$glassOff " +
                    "count=${materialInstances.size}",
            )
            swapMaterials = null
            return@LaunchedEffect
        }
        VitrinaDebugLog.d(
            "VitrinaMaterials",
            "Materiales listos ON/OFF + GLASS + lamps " +
                "(led=${lampLed != null} interior=${lampInterior != null} count=${materialInstances.size})",
        )
        // No mutar los materiales ON del GLB (setParameter puede abortar Filament).
        // OFF = clon con emisión en 0; si falla el clon, Intro sigue sin lámparas dinámicas.
        val lampLedOff = runCatching {
            duplicateLampOff(lampLed, "${VitrinaConstants.LAMP_LED_MATERIAL}_OFF")
        }.getOrNull()
        val lampInteriorOff = runCatching {
            duplicateLampOff(lampInterior, "${VitrinaConstants.LAMP_INTERIOR_MATERIAL}_OFF")
        }.getOrNull()
        swapMaterials = VitrinaSwapMaterials(
            matOn = matOn!!,
            matOff = matOff!!,
            glassOn = glassOn!!,
            glassOff = glassOff!!,
            lampLedOn = if (lampLedOff != null) lampLed else null,
            lampLedOff = lampLedOff,
            lampInteriorOn = if (lampInteriorOff != null) lampInterior else null,
            lampInteriorOff = lampInteriorOff,
        )
    }

    LaunchedEffect(baseInstance, swapMaterials, activeIndex, isUserActive, targetRotationDegrees) {
        val instance = baseInstance ?: return@LaunchedEffect
        val mats = swapMaterials ?: return@LaunchedEffect
        val renderableManager = engine.renderableManager

        // Solo reaccionar cuando cambia el estado relevante (no cada frame del Animatable).
        snapshotFlow {
            // Tras idle infinito el Animatable puede estar en −360/−720/…; hay que
            // comparar con el target equivalente más cercano o nunca llega a “close”.
            val alignedTarget = VitrinaRotation.nearestEquivalentAngle(
                rotationAnim.value,
                targetRotationDegrees,
            )
            // Umbral un poco amplio: en specialty/phq el settle a veces quedaba a ~1–2°
            // y la cara se quedaba apagada.
            val close = abs(rotationAnim.value - alignedTarget) < 2.5f
            // Luz = misma cara que rotación/nav (activeIndex). No usar front-by-bearing:
            // ese fallback reintroducía el cruce specialty↔phq.
            val activeNode = if (close && isUserActive) activeIndex else -1
            val glassLit = close && isUserActive
            Triple(activeNode, glassLit, close)
        }
            .distinctUntilChanged()
            .collect { (activeNodeIndex, glassLit, close) ->
                val litName = VitrinaConstants.UNIT_GLB_NODE_NAMES
                    .getOrNull(activeNodeIndex) ?: "none"
                val navId = if (activeNodeIndex >= 0) {
                    VitrinaGlbMapping.navigationUnitIdFor(activeNodeIndex)
                } else {
                    "none"
                }
                VitrinaDebugLog.d(
                    "VitrinaDiag",
                    "LIGHT close=$close userActive=$isUserActive activeIndex=$activeIndex " +
                        "litIndex=$activeNodeIndex litNode=$litName navId=$navId " +
                        "unitId=$activeUnitId rot=${"%.1f".format(rotationAnim.value)} " +
                        "target=${"%.1f".format(targetRotationDegrees)}",
                )
                try {
                    VitrinaConstants.UNIT_GLB_NODE_NAMES.forEachIndexed { index, nodeName ->
                        val entity = instance.model.getFirstEntityByName(nodeName)
                        if (entity != 0 && renderableManager.hasComponent(entity)) {
                            val ri = renderableManager.getInstance(entity)
                            if (ri != 0) {
                                val on = index == activeNodeIndex
                                val desired = if (on) mats.matOn else mats.matOff
                                renderableManager.setMaterialInstanceAt(ri, 0, desired)
                                if (index == activeNodeIndex || index == 2 || index == 3) {
                                    VitrinaDebugLog.d(
                                        "VitrinaDiag",
                                        "  set $nodeName entity=$entity → ${if (on) "ON" else "OFF"}",
                                    )
                                }
                            } else {
                                VitrinaDebugLog.w("VitrinaDiag", "  $nodeName ri=0")
                            }
                        } else {
                            VitrinaDebugLog.w(
                                "VitrinaDiag",
                                "  $nodeName entity=$entity missing renderable",
                            )
                        }
                    }
                    listOf("Cylinder", "Cylinder.002").forEach { nodeName ->
                        val entity = instance.model.getFirstEntityByName(nodeName)
                        if (entity != 0 && renderableManager.hasComponent(entity)) {
                            val ri = renderableManager.getInstance(entity)
                            if (ri != 0) {
                                val desired = if (glassLit) mats.glassOn else mats.glassOff
                                renderableManager.setMaterialInstanceAt(ri, 0, desired)
                            }
                        }
                    }
                } catch (t: Throwable) {
                    // No tumbar Intro en Fire/low-end si Filament rechaza un swap.
                    VitrinaDebugLog.e(
                        "VitrinaDiag",
                        "Material swap failed lit=$litName glassLit=$glassLit",
                        t,
                    )
                }
                // Lámparas aisladas: un fallo aquí no debe romper cintillo/acrílico.
                try {
                    applyFaceLamps(
                        instance = instance,
                        renderableManager = renderableManager,
                        mats = mats,
                        litFaceIndex = activeNodeIndex,
                    )
                } catch (t: Throwable) {
                    VitrinaDebugLog.e(
                        "VitrinaDiag",
                        "Lamp swap failed litIndex=$activeNodeIndex",
                        t,
                    )
                }
            }
    }

    Box(modifier = modifier) {
        SceneView(
            modifier = Modifier
                .matchParentSize()
                .modalBackdropBlur(backdropBlurred),
            surfaceType = SurfaceType.TextureSurface,
            renderQuality = renderQuality,
            engine = engine,
            modelLoader = modelLoader,
            environmentLoader = environmentLoader,
            environment = environment,
            cameraNode = cameraNode,
            mainLightNode = mainLightNode,
            cameraManipulator = null,
            lifecycle = sceneLifecycle,
            isOpaque = false,
            autoCenterContent = false,
            content = {
                baseInstance?.let { instance ->
                    ModelNode(
                        modelInstance = instance,
                        autoAnimate = false,
                        scaleToUnits = null,
                        centerOrigin = null,
                        scale = Scale(scene.uniformScale),
                        rotation = staticBaseRotation,
                        position = Position(y = baseY),
                        isEditable = false,
                        apply = { baseRotationHandle.bind(this) },
                    )
                    SideEffect {
                        baseRotationHandle.reapply()
                    }
                }
            },
        )

        if (baseInstance == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = LaSanteGreen,
            )
        }
    }
}

private fun projectUnitAnchorCenters(
    anchors: List<VitrinaResolvedUnitAnchor>,
    scene: VitrinaSceneMetrics,
    baseY: Float,
    rotationAngle: Float,
    aspectRatio: Float,
): Map<Int, FeaturedSlotScreenPoint> {
    if (anchors.isEmpty()) return emptyMap()

    val result = linkedMapOf<Int, FeaturedSlotScreenPoint>()
    anchors.forEach { anchor ->
        val point = VitrinaScreenProjection.projectPoint(
            slotGltf = anchor.position,
            scene = scene,
            baseY = baseY,
            rotationDegrees = rotationAngle,
            aspectRatio = aspectRatio,
        ) ?: return@forEach
        result[anchor.index] = point
    }
    return result
}
