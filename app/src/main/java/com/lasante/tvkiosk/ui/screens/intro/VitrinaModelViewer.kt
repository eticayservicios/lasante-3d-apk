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
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import com.lasante.tvkiosk.ui.utils.modalBackdropBlur
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.createEnvironment
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
import com.google.android.filament.MaterialInstance

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
    // Entorno/luz por defecto de SceneView: sin IBL custom, sin teñir el albedo del GLB.
    val environment = rememberEnvironment(environmentLoader, isOpaque = false) {
        createEnvironment(environmentLoader, isOpaque = false).let { env ->
            env.skybox?.let { engine.safeDestroySkybox(it) }
            Environment(
                indirectLight = env.indirectLight,
                skybox = null,
                sphericalHarmonics = env.sphericalHarmonics,
            )
        }
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
    LaunchedEffect(targetRotationDegrees, rotationAnimationSpec, sceneActive, isDragging, isUserActive) {
        if (isDragging) return@LaunchedEffect
        if (!sceneActive) {
            rotationAnim.snapTo(targetRotationDegrees)
            baseRotationHandle.setBaseDegrees(targetRotationDegrees)
            return@LaunchedEffect
        }
        if (!isUserActive) {
            // Rotación continua, lenta e infinita cuando el usuario está inactivo.
            // 360 grados cada 30 segundos = velocidad lenta, uniforme y constante en todos los dispositivos.
            val startAngle = rotationAnim.value
            rotationAnim.animateTo(
                targetValue = startAngle - 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 30000, easing = LinearEasing),
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

    // Intercambio dinámico de materiales para el cintillo y el acrílico de la vitrina.
    // Durante el giro constante lento:
    // - Las secciones del cintillo se muestran apagadas (MAT_SECCION_OFF).
    // - El acrílico de la vitrina se muestra apagado (material GLASS tradicional con opacidad esmerilada de piso).
    // Al detenerse por touch:
    // - La sección activa del cintillo se ilumina con MAT_SECCION_ON.
    // - El acrílico cambia a GLASS.on (con brillo sutil emisivo para indicar que la vitrina está encendida/activa).
    LaunchedEffect(baseInstance, activeIndex, rotationAnim.value, targetRotationDegrees, isUserActive) {
        val instance = baseInstance ?: return@LaunchedEffect
        val renderableManager = engine.renderableManager

        var matOn: MaterialInstance? = null
        var matOff: MaterialInstance? = null
        var glassOn: MaterialInstance? = null
        var glassOff: MaterialInstance? = null

        val materialInstances = instance.getMaterialInstances()
        materialInstances.forEachIndexed { idx, mat ->
            val name = try { mat.getName() ?: "" } catch (e: Exception) { "" }
            
            android.util.Log.d("VitrinaMaterials", "MaterialInstance[$idx]: name='$name'")
            
            if (name == "MAT_SECCION_ON") matOn = mat
            if (name == "MAT_SECCION_OFF") matOff = mat
            if (name == "GLASS.on") glassOn = mat
            if (name == "GLASS") glassOff = mat
        }

        // Fallback por índice estable si la API de nombres de gltfio devuelve vacío (seguridad 100% infalible)
        if (matOn == null && materialInstances.size > 18) {
            matOn = materialInstances[18]
            android.util.Log.w("VitrinaMaterials", "Fallback index 18 usado para MAT_SECCION_ON")
        }
        if (matOff == null && materialInstances.size > 19) {
            matOff = materialInstances[19]
            android.util.Log.w("VitrinaMaterials", "Fallback index 19 usado para MAT_SECCION_OFF")
        }
        if (glassOn == null && materialInstances.size > 17) {
            glassOn = materialInstances[17]
            android.util.Log.w("VitrinaMaterials", "Fallback index 17 usado para GLASS.on")
        }
        if (glassOff == null && materialInstances.size > 16) {
            glassOff = materialInstances[16]
            android.util.Log.w("VitrinaMaterials", "Fallback index 16 usado para GLASS")
        }

        if (matOn == null || matOff == null || glassOn == null || glassOff == null) {
            android.util.Log.e("VitrinaMaterials", "ERROR: No se pudieron encontrar todos los materiales! matOn=$matOn, matOff=$matOff, glassOn=$glassOn, glassOff=$glassOff")
            return@LaunchedEffect
        }

        val isCloseToTarget = kotlin.math.abs(rotationAnim.value - targetRotationDegrees) < 1.0f
        val activeNodeIndex = if (isCloseToTarget && isUserActive) activeIndex else -1

        android.util.Log.d(
            "VitrinaMaterials",
            "Swapping... isUserActive=$isUserActive, isCloseToTarget=$isCloseToTarget, activeIndex=$activeIndex -> activeNodeIndex=$activeNodeIndex"
        )

        // 1. Alternar cintillo
        VitrinaConstants.UNIT_GLB_NODE_NAMES.forEachIndexed { index, nodeName ->
            val entity = instance.model.getFirstEntityByName(nodeName)
            if (entity != 0 && renderableManager.hasComponent(entity)) {
                val renderableInstance = renderableManager.getInstance(entity)
                if (renderableInstance != 0) {
                    val desiredMat = if (index == activeNodeIndex) matOn!! else matOff!!
                    renderableManager.setMaterialInstanceAt(renderableInstance, 0, desiredMat)
                }
            }
        }

        // 2. Alternar vidrio/acrílico (GLASS cuando gira, GLASS.on cuando se detiene)
        // Cylinder (nodo 40) y Cylinder.002 (nodo 41) representan las partes de acrílico de la vitrina
        listOf("Cylinder", "Cylinder.002").forEach { nodeName ->
            val entity = instance.model.getFirstEntityByName(nodeName)
            if (entity != 0 && renderableManager.hasComponent(entity)) {
                val renderableInstance = renderableManager.getInstance(entity)
                if (renderableInstance != 0) {
                    val desiredGlass = if (isCloseToTarget && isUserActive) glassOn!! else glassOff!!
                    renderableManager.setMaterialInstanceAt(renderableInstance, 0, desiredGlass)
                    android.util.Log.d("VitrinaMaterials", "Set cylinder '$nodeName' to material: " + (if (desiredGlass === glassOn) "GLASS.on" else "GLASS"))
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
