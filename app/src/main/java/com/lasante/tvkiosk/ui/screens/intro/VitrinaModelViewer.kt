package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
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
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.safeDestroySkybox

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

    val mainLightNode = rememberMainLightNode(engine) {
        intensity = scene.lightIntensity
    }

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
    LaunchedEffect(targetRotationDegrees, rotationAnimationSpec, sceneActive) {
        if (!sceneActive) {
            rotationAnim.snapTo(targetRotationDegrees)
            baseRotationHandle.setBaseDegrees(targetRotationDegrees)
            return@LaunchedEffect
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
