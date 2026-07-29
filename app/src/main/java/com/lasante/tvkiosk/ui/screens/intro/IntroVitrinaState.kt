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
    var mode by remember { mutableStateOf(VitrinaMode.Interactive) }
    var rotationAnimationSpec by remember {
        mutableStateOf<AnimationSpec<Float>>(VitrinaConstants.manualRotationAnimationSpec)
    }
    var lastUserInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var dragAccumulatedX by remember { mutableFloatStateOf(0f) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val isTabletLandscape = configuration.screenWidthDp > configuration.screenHeightDp &&
        configuration.screenWidthDp >= 640 &&
        configuration.screenHeightDp >= 400
    val isPhoneLandscape = configuration.screenWidthDp > configuration.screenHeightDp &&
        configuration.screenHeightDp < 520 &&
        !isTabletLandscape
    val isTvLandscape = configuration.screenWidthDp >= 880 &&
        configuration.screenHeightDp >= 480 &&
        !isPhoneLandscape
    val dragSnapThresholdPx = remember(isTabletLandscape, isPhoneLandscape, isTvLandscape, density) {
        with(density) {
            when {
                // TV antes que tablet: ~961×529 es ambos.
                isTvLandscape -> 30.dp.toPx()
                isPhoneLandscape -> 22.dp.toPx()
                isTabletLandscape -> 30.dp.toPx()
                else -> 26.dp.toPx()
            }
        }
    }
    val dragDegreesPerPx = VitrinaConstants.ROTATION_STEP_DEGREES / dragSnapThresholdPx
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

    fun dragVisualOffsetDegrees(): Float {
        val offset = -dragAccumulatedX * dragDegreesPerPx
        return offset.coerceIn(
            -VitrinaConstants.ROTATION_STEP_DEGREES,
            VitrinaConstants.ROTATION_STEP_DEGREES,
        )
    }

    fun updateDragVisual() {
        baseRotationHandle?.setDragOffset(dragVisualOffsetDegrees())
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
        return VitrinaRotation.rotationForActiveUnit(safeIndex, emptyList())
    }

    fun settleInteractive() {
        mode = VitrinaMode.Interactive
        lastUserInteraction = System.currentTimeMillis()
    }

    fun registerInteraction() {
        lastUserInteraction = System.currentTimeMillis()
        if (!hasModalOpen && mode != VitrinaMode.Dragging) {
            mode = VitrinaMode.Interactive
        }
    }

    fun dismissScreenSaver() {
        lastUserInteraction = System.currentTimeMillis()
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
        if (mode == VitrinaMode.Dragging) {
            dragAccumulatedX += deltaX
            updateDragVisual()
            lastUserInteraction = System.currentTimeMillis()
        }
    }

    fun handleDragEnd() {
        if (itemCount <= 0 || hasModalOpen || mode != VitrinaMode.Dragging) return
        val totalDeltaX = dragAccumulatedX
        baseRotationHandle?.clearDragOffset()
        // Snap siempre a ±1 paso (igual que botón Rota)
        val steps = when {
            totalDeltaX < -dragSnapThresholdPx -> 1
            totalDeltaX > dragSnapThresholdPx -> -1
            else -> 0
        }
        dragAccumulatedX = 0f
        android.util.Log.d(
            "VitrinaGesture",
            "drag end deltaX=$totalDeltaX steps=$steps activeIndex=$activeIndex",
        )
        if (steps == 0) settleInteractive() else rotateBySteps(steps, source = "drag")
    }

    LaunchedEffect(itemCount, activeIndex) {
        if (itemCount > 0) {
            displayRotationDegrees = rotationForIndex(activeIndex)
        }
    }

    LaunchedEffect(hasModalOpen) {
        if (hasModalOpen) {
            mode = VitrinaMode.ModalOpen
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
                    delay(1000L)
                }
                idleFor >= autoRotateAfterMs -> {
                    if (mode != VitrinaMode.Dragging && mode != VitrinaMode.ScreenSaver) {
                        rotateBySteps(1, source = "auto")
                    }
                    delay(AUTO_ROTATE_STEP_MS)
                }
                else -> {
                    if (mode != VitrinaMode.Interactive) {
                        mode = VitrinaMode.Interactive
                    }
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
