package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private const val DEFAULT_AUTO_ROTATE_TIMEOUT_MS = 2 * 60 * 1000L
private const val DEFAULT_SCREEN_SAVER_TIMEOUT_MS = 3 * 60 * 1000L
private const val AUTO_ROTATE_STEP_MS = 4_000L

private enum class VitrinaMode {
    ScreenSaver,
    AutoRotating,
    Interactive,
    Dragging,
    ModalOpen,
}

@Stable
class VitrinaInteractionController internal constructor(
    val activeIndex: Int,
    /** Grados canónicos de rotación Y; siempre alineados con [activeIndex]. */
    val displayRotationDegrees: Float,
    val isDragging: Boolean,
    /** Productos visibles y clickeables — solo en unidad detenida al frente. */
    val showProducts: Boolean,
    val isScreenSaverActive: Boolean,
    val isUserActive: Boolean,
    val rotationAnimationSpec: AnimationSpec<Float>,
    val registerInteraction: () -> Unit,
    val dismissScreenSaver: () -> Unit,
    val onRotationAnimationFinished: () -> Unit,
    val rotateOnce: () -> Unit,
    val selectUnit: (Int) -> Unit,
    val onDragStart: () -> Unit,
    val onDrag: (Float) -> Unit,
    val onDragEnd: () -> Unit,
)

@Composable
fun rememberVitrinaInteractionController(
    itemCount: Int,
    hasModalOpen: Boolean,
    baseRotationHandle: VitrinaBaseRotationHandle? = null,
    contentActive: Boolean = true,
    screenSaverEnabled: Boolean = true,
    autoRotateAfterMs: Long = DEFAULT_AUTO_ROTATE_TIMEOUT_MS,
    screenSaverAfterMs: Long = DEFAULT_SCREEN_SAVER_TIMEOUT_MS,
): VitrinaInteractionController {
    var activeIndex by rememberSaveable { mutableIntStateOf(0) }
    var displayRotationDegrees by rememberSaveable { mutableFloatStateOf(0f) }
    var mode by remember { mutableStateOf(VitrinaMode.AutoRotating) }
    var rotationAnimationSpec by remember {
        mutableStateOf<AnimationSpec<Float>>(VitrinaConstants.manualRotationAnimationSpec)
    }
    var isUserActive by remember { mutableStateOf(false) }
    var lastUserInteraction by remember { mutableLongStateOf(System.currentTimeMillis() - autoRotateAfterMs) }
    var dragAccumulatedX by remember { mutableFloatStateOf(0f) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    // Arrastre directo: ~40% del ancho de pantalla ≈ 1 unidad (72°).
    val dragDegreesPerPx = remember(configuration.screenWidthDp, density) {
        val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
        val trackWidthPx = (screenWidthPx * VitrinaConstants.DRAG_TRACK_WIDTH_SCREEN_FRACTION)
            .coerceAtLeast(1f)
        VitrinaConstants.ROTATION_STEP_DEGREES / trackWidthPx
    }
    var isForeground by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    val isActive = isForeground && contentActive
    var resumedFromBackground by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    isForeground = true
                    if (resumedFromBackground) {
                        lastUserInteraction = System.currentTimeMillis()
                        if (mode == VitrinaMode.ScreenSaver || mode == VitrinaMode.AutoRotating) {
                            mode = VitrinaMode.Interactive
                        }
                        resumedFromBackground = false
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    isForeground = false
                    resumedFromBackground = true
                    if (mode == VitrinaMode.ScreenSaver) {
                        mode = VitrinaMode.Interactive
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isDragging = mode == VitrinaMode.Dragging

    /**
     * Offset Y del mesh mientras se arrastra.
     * Index+1 usa rotación más negativa (−72°); swipe izquierda (deltaX < 0) debe
     * bajar el ángulo para previsualizar la unidad siguiente (no la anterior).
     */
    fun dragVisualOffsetDegrees(): Float = dragAccumulatedX * dragDegreesPerPx

    fun updateDragVisual() {
        baseRotationHandle?.setDragOffset(dragVisualOffsetDegrees())
    }

    /**
     * Pasos de [activeIndex]: opuesto al offset de rotación porque
     * subir de índice baja el ángulo Y del modelo.
     */
    fun dragStepsFromOffset(offsetDegrees: Float): Int {
        val step = VitrinaConstants.ROTATION_STEP_DEGREES
        val indexSpaceDegrees = -offsetDegrees
        if (abs(indexSpaceDegrees) < step * VitrinaConstants.DRAG_SNAP_COMMIT_FRACTION) return 0
        val raw = indexSpaceDegrees.div(step).roundToInt()
        val maxSteps = (itemCount - 1).coerceAtLeast(1)
        return raw.coerceIn(-maxSteps, maxSteps)
    }

    /** Cancela drag y restaura el cilindro a la unidad activa (p. ej. modal a mitad de gesto). */
    fun abortDragToCurrentUnit() {
        if (mode != VitrinaMode.Dragging) return
        dragAccumulatedX = 0f
        baseRotationHandle?.clearDragOffset()
        baseRotationHandle?.setBaseDegrees(displayRotationDegrees)
        mode = if (hasModalOpen) VitrinaMode.ModalOpen else VitrinaMode.Interactive
        lastUserInteraction = System.currentTimeMillis()
    }

    // Productos siempre visibles salvo screen saver.
    val showProducts = mode != VitrinaMode.ScreenSaver

    fun onRotationAnimationFinished() {
        if (!hasModalOpen && mode == VitrinaMode.AutoRotating) {
            mode = VitrinaMode.Interactive
        }
    }

    fun normalizeIndex(index: Int): Int {
        if (itemCount <= 0) return 0
        return ((index % itemCount) + itemCount) % itemCount
    }

    fun rotationForIndex(index: Int): Float {
        val safeIndex = normalizeIndex(index)
        return VitrinaRotation.modelRotationYForBearing(
            VitrinaGlbMapping.bearingFor(safeIndex),
        )
    }

    fun settleInteractive() {
        mode = VitrinaMode.Interactive
        isUserActive = true
        lastUserInteraction = System.currentTimeMillis()
    }

    fun registerInteraction() {
        lastUserInteraction = System.currentTimeMillis()
        isUserActive = true
        if (!hasModalOpen && mode != VitrinaMode.Dragging) {
            mode = VitrinaMode.Interactive
        }
    }

    fun dismissScreenSaver() {
        lastUserInteraction = System.currentTimeMillis()
        isUserActive = true
        if (mode == VitrinaMode.ScreenSaver) {
            mode = VitrinaMode.Interactive
        }
    }

    fun rotateBySteps(steps: Int, source: String) {
        if (itemCount <= 0 || steps == 0 || hasModalOpen) return
        val previousIndex = activeIndex
        val nextIndex = normalizeIndex(activeIndex + steps)
        activeIndex = nextIndex
        displayRotationDegrees = rotationForIndex(nextIndex)
        android.util.Log.d(
            "VitrinaGesture",
            "rotate source=$source steps=$steps prevIndex=$previousIndex " +
                "nextIndex=$nextIndex itemCount=$itemCount rotation=$displayRotationDegrees",
        )
        when (source) {
            "screensaver" -> mode = VitrinaMode.ScreenSaver
            "auto" -> {
                rotationAnimationSpec = VitrinaConstants.autoRotationAnimationSpec
                mode = VitrinaMode.AutoRotating
            }
            else -> {
                rotationAnimationSpec = VitrinaConstants.manualRotationAnimationSpec
                lastUserInteraction = System.currentTimeMillis()
                mode = VitrinaMode.AutoRotating
            }
        }
    }

    fun selectUnit(targetIndex: Int) {
        if (itemCount <= 0 || hasModalOpen) return
        val safeTarget = normalizeIndex(targetIndex)
        if (safeTarget == activeIndex) {
            registerInteraction()
            return
        }
        var delta = safeTarget - activeIndex
        if (delta > itemCount / 2) delta -= itemCount
        if (delta < -itemCount / 2) delta += itemCount
        rotateBySteps(delta, source = "select")
    }

    fun handleDragStart() {
        if (itemCount <= 0 || hasModalOpen || mode == VitrinaMode.Dragging) return
        lastUserInteraction = System.currentTimeMillis()
        dragAccumulatedX = 0f
        baseRotationHandle?.clearDragOffset()
        mode = VitrinaMode.Dragging
        android.util.Log.d(
            "VitrinaGesture",
            "drag start activeIndex=$activeIndex rotation=$displayRotationDegrees",
        )
    }

    fun handleDrag(deltaX: Float) {
        if (mode != VitrinaMode.Dragging) return
        dragAccumulatedX += deltaX
        updateDragVisual()
    }

    fun handleDragEnd() {
        if (mode != VitrinaMode.Dragging) return
        if (itemCount <= 0 || hasModalOpen) {
            abortDragToCurrentUnit()
            return
        }
        val offsetDegrees = dragVisualOffsetDegrees()
        val steps = dragStepsFromOffset(offsetDegrees)
        dragAccumulatedX = 0f
        baseRotationHandle?.commitDragIntoBase()
        android.util.Log.d(
            "VitrinaGesture",
            "drag end offsetDeg=$offsetDegrees steps=$steps activeIndex=$activeIndex",
        )
        if (steps == 0) {
            settleInteractive()
        } else {
            rotateBySteps(steps, source = "drag")
        }
    }

    LaunchedEffect(itemCount, activeIndex) {
        if (itemCount > 0) {
            displayRotationDegrees = rotationForIndex(activeIndex)
        }
    }

    LaunchedEffect(hasModalOpen) {
        if (hasModalOpen) {
            if (mode == VitrinaMode.Dragging) {
                abortDragToCurrentUnit()
            } else {
                mode = VitrinaMode.ModalOpen
            }
        } else if (mode == VitrinaMode.ModalOpen) {
            settleInteractive()
        }
    }

    LaunchedEffect(itemCount, hasModalOpen, screenSaverEnabled, isActive) {
        while (true) {
            if (!isActive) {
                if (mode == VitrinaMode.ScreenSaver) {
                    mode = VitrinaMode.Interactive
                }
                delay(500L)
                continue
            }

            val now = System.currentTimeMillis()
            if (hasModalOpen) {
                delay(500L)
                continue
            }

            if (mode == VitrinaMode.Dragging) {
                delay(200L)
                continue
            }

            val idleFor = now - lastUserInteraction
            when {
                screenSaverEnabled && idleFor >= screenSaverAfterMs -> {
                    if (mode != VitrinaMode.ScreenSaver) {
                        android.util.Log.i(
                            "VitrinaScreenSaver",
                            "Activando screen saver idleFor=${idleFor}ms umbral=${screenSaverAfterMs}ms",
                        )
                        mode = VitrinaMode.ScreenSaver
                    }
                    isUserActive = false
                    delay(1000L)
                }
                idleFor >= autoRotateAfterMs -> {
                    if (mode != VitrinaMode.Dragging && mode != VitrinaMode.ScreenSaver) {
                        if (mode != VitrinaMode.AutoRotating) {
                            mode = VitrinaMode.AutoRotating
                        }
                    }
                    isUserActive = false
                    delay(1000L)
                }
                else -> {
                    if (mode != VitrinaMode.Interactive) {
                        mode = VitrinaMode.Interactive
                    }
                    isUserActive = true
                    delay(1000L)
                }
            }
        }
    }

    return VitrinaInteractionController(
        activeIndex = activeIndex,
        displayRotationDegrees = displayRotationDegrees,
        isDragging = isDragging,
        showProducts = showProducts,
        isScreenSaverActive = !hasModalOpen && mode == VitrinaMode.ScreenSaver,
        isUserActive = isUserActive,
        rotationAnimationSpec = rotationAnimationSpec,
        registerInteraction = ::registerInteraction,
        dismissScreenSaver = ::dismissScreenSaver,
        onRotationAnimationFinished = ::onRotationAnimationFinished,
        rotateOnce = { rotateBySteps(1, source = "button") },
        selectUnit = ::selectUnit,
        onDragStart = ::handleDragStart,
        onDrag = ::handleDrag,
        onDragEnd = ::handleDragEnd,
    )
}
