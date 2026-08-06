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
import io.github.sceneview.model.model
import io.github.sceneview.node.ModelNode
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
)@Composable
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
        val materialInstances = instance.getMaterialInstances()
        materialInstances.forEachIndexed { idx, mat ->
            val name = try { mat.getName() ?: "" } catch (_: Exception) { "" }
            android.util.Log.d("VitrinaMaterials", "MaterialInstance[$idx]: name='$name'")
            when (name) {
                "MAT_SECCION_ON" -> matOn = mat
                "MAT_SECCION_OFF" -> matOff = mat
                "GLASS.on" -> glassOn = mat
                "GLASS" -> glassOff = mat
            }
        }
        if (matOn == null || matOff == null || glassOn == null || glassOff == null) {
            android.util.Log.e(
                "VitrinaMaterials",
                "Materiales incompletos (¿OFF sin mesh en GLB?). " +
                    "matOn=$matOn matOff=$matOff glassOn=$glassOn glassOff=$glassOff " +
                    "count=${materialInstances.size}",
            )
            swapMaterials = null
            return@LaunchedEffect
        }
        android.util.Log.i(
            "VitrinaMaterials",
            "Materiales listos ON/OFF + GLASS/GLASS.on (count=${materialInstances.size})",
        )
        swapMaterials = VitrinaSwapMaterials(matOn!!, matOff!!, glassOn!!, glassOff!!)
    }

    LaunchedEffect(baseInstance, swapMaterials, activeIndex, isUserActive, targetRotationDegrees, unitAnchors) {
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
            val close = abs(rotationAnim.value - alignedTarget) < 1.0f
            // Encender el mesh cuya bearing queda al frente (no confiar solo en activeIndex
            // si hubo desfase angular specialty/phq).
            val frontIndex = if (unitAnchors.isNotEmpty()) {
                unitAnchors.minByOrNull { anchor ->
                    val faceRot = VitrinaRotation.modelRotationYForBearing(anchor.bearingDegrees)
                    val aligned = VitrinaRotation.nearestEquivalentAngle(rotationAnim.value, faceRot)
                    abs(rotationAnim.value - aligned)
                }?.index ?: activeIndex
            } else {
                activeIndex
            }
            // Idle auto-giro → todo apagado. Usuario activo + cara al frente → ON.
            val activeNode = if (close && isUserActive) frontIndex else -1
            val glassLit = close && isUserActive
            Triple(activeNode, glassLit, close)
        }
            .distinctUntilChanged()
            .collect { (activeNodeIndex, glassLit, close) ->
                val litName = VitrinaConstants.UNIT_GLB_NODE_NAMES
                    .getOrNull(activeNodeIndex) ?: "none"
                android.util.Log.d(
                    "VitrinaMaterials",
                    "Swap activeNode=$activeNodeIndex lit=$litName glassLit=$glassLit " +
                        "close=$close userActive=$isUserActive activeIndex=$activeIndex " +
                        "rot=${"%.1f".format(rotationAnim.value)} " +
                        "target=${"%.1f".format(targetRotationDegrees)}",
                )
                VitrinaConstants.UNIT_GLB_NODE_NAMES.forEachIndexed { index, nodeName ->
                    val entity = instance.model.getFirstEntityByName(nodeName)
                    if (entity != 0 && renderableManager.hasComponent(entity)) {
                        val ri = renderableManager.getInstance(entity)
                        if (ri != 0) {
                            val desired = if (index == activeNodeIndex) mats.matOn else mats.matOff
                            renderableManager.setMaterialInstanceAt(ri, 0, desired)
                        }
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
