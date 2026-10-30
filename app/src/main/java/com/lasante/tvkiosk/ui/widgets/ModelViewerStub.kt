package com.lasante.tvkiosk.ui.widgets

import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.filament.Engine
import com.google.android.filament.utils.Manipulator
import com.lasante.tvkiosk.app.FilamentNativeSupport
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.lookAt
import io.github.sceneview.SceneView
import io.github.sceneview.RenderQuality
import io.github.sceneview.gesture.CameraGestureDetector
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Transform
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sign
import kotlin.math.sqrt

/** Ángulo Y inicial del producto en el modal (3/4 / medio lado, no frontal). */
private const val PRODUCT_MODAL_INITIAL_YAW_DEG = 35f
/** Distancia de cámara por defecto. */
private const val PRODUCT_MODAL_CAMERA_Z = 2.8f

/**
 * Curva base SceneView + ganancia baja: sin esto, ~100px de pellizco
 * metían delta≈-1.4 y saltaba 2.8→0.55 en un frame (logs).
 */
private const val PRODUCT_MODAL_PINCH_ZOOM_SPEED = 1f / 18f
private const val PRODUCT_MODAL_PINCH_ZOOM_DAMPING = 0.7f
/** Escala el delta de SceneView a unidades de distancia de cámara. */
private const val PRODUCT_MODAL_DOLLY_GAIN = 0.20f
/** Tope de cambio por frame → zoom continuo, no “de golpe”. */
private const val PRODUCT_MODAL_MAX_DOLLY_STEP = 0.20f
/** Ignora micro-temblores de dedos (px de separación). */
private const val PRODUCT_MODAL_PINCH_DEADZONE_PX = 8f

/** Menor = producto más grande; mayor = más pequeño. */
private const val PRODUCT_MODAL_MIN_CAMERA_DIST = 0.50f
private const val PRODUCT_MODAL_MAX_CAMERA_DIST = 4.8f

private const val TAG = "ProductModal3D"

/**
 * Visualizador 3D de producto (assets locales o URLs remotas CloudFront).
 *
 * Órbita = Manipulator Filament. Zoom = distancia propia con clamp duro
 * (sin “deuda” al tope: pellizcar al revés responde al instante).
 */
@Composable
fun ModelViewerStub(
    modifier: Modifier = Modifier,
    modelUrl: String?,
    scaleToUnits: Float = 1.0f,
    cameraZ: Float = PRODUCT_MODAL_CAMERA_Z,
) {
    if (modelUrl.isNullOrBlank()) return

    val cleanPath = remember(modelUrl) {
        when {
            modelUrl.startsWith("file:///android_asset/") -> modelUrl.removePrefix("file:///android_asset/")
            modelUrl.startsWith("./") -> modelUrl.removePrefix("./")
            else -> modelUrl.trim()
        }
    }

    val context = LocalContext.current
    val engine = rememberEngine { eglContext ->
        if (FilamentNativeSupport.requiresVulkanBackend()) {
            Engine.create(Engine.Backend.VULKAN)
        } else {
            Engine.create(eglContext)
        }
    }
    val modelLoader = rememberModelLoader(engine, context)
    val modelInstance = rememberProductModelInstance(modelLoader, cleanPath)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 0f, z = cameraZ)
        lookAt(Position(x = 0f, y = 0f, z = 0f), smooth = false)
    }
    LaunchedEffect(cameraZ) {
        cameraNode.position = Position(x = 0f, y = 0f, z = cameraZ)
        cameraNode.lookAt(Position(x = 0f, y = 0f, z = 0f), smooth = false)
    }
    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 100_000f
    }

    // Misma posición que antes del fix de zoom: centrado en el slot.
    // El look-at fijo al pivote (0,0,0) en getTransform evita el “chiquito” al girar.
    val orbitHome = remember(cameraZ) { Position(x = 0f, y = 0f, z = cameraZ) }
    val orbitTarget = remember { Position(x = 0f, y = 0f, z = 0f) }
    val cameraManipulator = remember(orbitHome, cameraZ) {
        ProductModalCameraManipulator(
            orbitHomePosition = orbitHome,
            targetPosition = orbitTarget,
            initialDistance = cameraZ.coerceIn(
                PRODUCT_MODAL_MIN_CAMERA_DIST,
                PRODUCT_MODAL_MAX_CAMERA_DIST,
            ),
            minDistance = PRODUCT_MODAL_MIN_CAMERA_DIST,
            maxDistance = PRODUCT_MODAL_MAX_CAMERA_DIST,
            pinchZoomSpeed = PRODUCT_MODAL_PINCH_ZOOM_SPEED,
            pinchZoomDamping = PRODUCT_MODAL_PINCH_ZOOM_DAMPING,
        )
    }

    LaunchedEffect(cleanPath, modelInstance != null) {
        Log.i(
            TAG,
            "viewer path=$cleanPath loaded=${modelInstance != null} " +
                "scaleToUnits=$scaleToUnits cameraZ=$cameraZ " +
                "pinchSpeed=$PRODUCT_MODAL_PINCH_ZOOM_SPEED damping=$PRODUCT_MODAL_PINCH_ZOOM_DAMPING " +
                "dollyGain=$PRODUCT_MODAL_DOLLY_GAIN maxStep=$PRODUCT_MODAL_MAX_DOLLY_STEP " +
                "distClamp=$PRODUCT_MODAL_MIN_CAMERA_DIST..$PRODUCT_MODAL_MAX_CAMERA_DIST ownedDolly=true",
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            renderQuality = RenderQuality.Performance,
            engine = engine,
            modelLoader = modelLoader,
            isOpaque = false,
            autoCenterContent = true,
            autoFitContent = false,
            cameraNode = cameraNode,
            mainLightNode = mainLightNode,
            cameraManipulator = cameraManipulator,
            onTouchEvent = { event, hit ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN ->
                        Log.d(
                            TAG,
                            "touch DOWN pointers=${event.pointerCount} " +
                                "hit=${hit?.node?.name ?: "none"} dist=${cameraManipulator.dollyDistance}",
                        )
                    MotionEvent.ACTION_POINTER_DOWN ->
                        Log.d(
                            TAG,
                            "touch PINCH_START pointers=${event.pointerCount} " +
                                "dist=${cameraManipulator.dollyDistance}",
                        )
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                        Log.d(
                            TAG,
                            "touch END action=${event.actionMasked} dist=${cameraManipulator.dollyDistance}",
                        )
                }
                false
            },
        ) {
            modelInstance?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    centerOrigin = Position(x = 0f, y = -1f, z = 0f),
                    scaleToUnits = scaleToUnits,
                    rotation = Rotation(y = PRODUCT_MODAL_INITIAL_YAW_DEG),
                    isEditable = false,
                )
            }
        }

        if (modelInstance == null) {
            CircularProgressIndicator(color = LaSanteGreen)
        }
    }
}

/**
 * Órbita via Filament; zoom via [dollyDistance] propia.
 * SceneView aplica [getTransform] cada frame — ahí forzamos la distancia clampada,
 * así no hay “deuda” cuando el pellizco interno pasaba del mínimo.
 */
private class ProductModalCameraManipulator(
    orbitHomePosition: Position,
    targetPosition: Position,
    initialDistance: Float,
    private val minDistance: Float,
    private val maxDistance: Float,
    private val pinchZoomSpeed: Float,
    private val pinchZoomDamping: Float,
) : CameraGestureDetector.CameraManipulator {
    private val manipulator: Manipulator = Manipulator.Builder()
        .orbitHomePosition(orbitHomePosition.x, orbitHomePosition.y, orbitHomePosition.z)
        .targetPosition(targetPosition.x, targetPosition.y, targetPosition.z)
        .orbitSpeed(0.003f, 0.003f)
        .zoomSpeed(0.05f)
        // Sin pan: un pellizco/desliz a 2 dedos no desplaza el pivote (producto “chiquito”).
        .panning(false)
        .build(Manipulator.Mode.ORBIT)

    private val eyeScratch = FloatArray(3)
    private val targetScratch = FloatArray(3)
    private val upScratch = FloatArray(3)

    /**
     * Pivote fijo del producto. Tras el primer orbit, Filament deja mTarget a ~1u del
     * eye (no en el pivote); si dolly-amos respecto a ese mTarget, el GLB se aleja.
     */
    private val pivotX = targetPosition.x
    private val pivotY = targetPosition.y
    private val pivotZ = targetPosition.z

    @Volatile
    var dollyDistance: Float = initialDistance
        private set

    override fun setViewport(width: Int, height: Int) {
        manipulator.setViewport(width, height)
    }

    override fun getTransform(): Transform {
        manipulator.getLookAt(eyeScratch, targetScratch, upScratch)
        // Siempre respecto al pivote del producto, no al mTarget de Filament.
        var dx = eyeScratch[0] - pivotX
        var dy = eyeScratch[1] - pivotY
        var dz = eyeScratch[2] - pivotZ
        var len = sqrt(dx * dx + dy * dy + dz * dz)
        if (len < 1e-5f) {
            dx = 0f
            dy = 0f
            dz = 1f
            len = 1f
        }
        val dist = dollyDistance.coerceIn(minDistance, maxDistance)
        val s = dist / len
        return lookAt(
            eye = Float3(pivotX + dx * s, pivotY + dy * s, pivotZ + dz * s),
            target = Float3(pivotX, pivotY, pivotZ),
            up = Float3(y = 1.0f),
        )
    }

    override fun grabBegin(x: Int, y: Int, strafe: Boolean) {
        manipulator.grabBegin(x, y, strafe)
    }

    override fun grabUpdate(x: Int, y: Int) {
        manipulator.grabUpdate(x, y)
    }

    override fun grabEnd() {
        manipulator.grabEnd()
    }

    override fun scrollBegin(x: Int, y: Int, separation: Float) = Unit

    override fun scrollUpdate(x: Int, y: Int, prevSeparation: Float, currSeparation: Float) {
        val sepDelta = abs(currSeparation - prevSeparation)
        if (sepDelta < PRODUCT_MODAL_PINCH_DEADZONE_PX) return

        val raw = pinchZoomDelta(
            prevSeparation,
            currSeparation,
            pinchZoomSpeed,
            pinchZoomDamping,
        ) * PRODUCT_MODAL_DOLLY_GAIN
        // delta < 0 (pinch out) → zoom in → distancia menor.
        val step = raw.coerceIn(-PRODUCT_MODAL_MAX_DOLLY_STEP, PRODUCT_MODAL_MAX_DOLLY_STEP)
        val before = dollyDistance
        dollyDistance = (dollyDistance + step).coerceIn(minDistance, maxDistance)
        if (abs(dollyDistance - before) > 0.001f) {
            Log.d(
                TAG,
                "pinch sep ${"%.1f".format(prevSeparation)}→${"%.1f".format(currSeparation)} " +
                    "step=${"%.3f".format(step)} dist ${"%.2f".format(before)}→${"%.2f".format(dollyDistance)}",
            )
        }
    }

    override fun scrollEnd() = Unit

    override fun update(deltaTime: Float) {
        manipulator.update(deltaTime)
    }
}

/** Copia local de la curva SceneView (el original es `internal`). */
private fun pinchZoomDelta(
    prevSeparation: Float,
    currSeparation: Float,
    speed: Float,
    damping: Float,
): Float {
    val delta = prevSeparation - currSeparation
    val absDelta = abs(delta)
    val damped = if (absDelta > 1f) {
        sign(delta) * exp(ln(absDelta.toDouble()) * damping).toFloat()
    } else {
        delta
    }
    return damped * speed
}

@Composable
private fun rememberProductModelInstance(
    modelLoader: io.github.sceneview.loaders.ModelLoader,
    path: String,
) = when {
    path.startsWith("http://") || path.startsWith("https://") ->
        rememberModelInstance(modelLoader, fileLocation = path)
    else ->
        rememberModelInstance(modelLoader, assetFileLocation = path)
}
