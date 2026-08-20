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
import kotlinx.coroutines.delay

private class IdleInteractionClock {
    var lastMs: Long = System.currentTimeMillis()
}

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
    /** Menor = más sensible. Ver [VitrinaConstants.dragTrackWidthScreenFraction]. */
    dragTrackWidthScreenFraction: Float = VitrinaConstants.DRAG_TRACK_WIDTH_SCREEN_FRACTION,
): VitrinaInteractionController {
    var activeIndex by rememberSaveable { mutableIntStateOf(0) }
    var displayRotationDegrees by rememberSaveable { mutableFloatStateOf(0f) }
    // Arranque quieto (luces ok) hasta que Filament cargue; luego idle gira sola.
    var mode by remember { mutableStateOf(VitrinaMode.Interactive) }
    var rotationAnimationSpec by remember {
        mutableStateOf<AnimationSpec<Float>>(VitrinaConstants.manualRotationAnimationSpec)
    }
    var isUserActive by remember { mutableStateOf(true) }
    /** Reloj de idle: no es Compose state. Si lo fuera, cada toque/tecla recompone Filament y crashea. */
    val idleClock = remember { IdleInteractionClock() }
    var dragAccumulatedX by remember { mutableFloatStateOf(0f) }
    var dragStartTimeMs by remember { mutableLongStateOf(0L) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    // Arrastre directo: fracción de ancho ≈ 1 unidad (72°). Menor fracción = más sensible.
    val dragDegreesPerPx = remember(configuration.screenWidthDp, density, dragTrackWidthScreenFraction) {
        val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
        val trackWidthPx = (screenWidthPx * dragTrackWidthScreenFraction)
            .coerceAtLeast(1f)
        VitrinaConstants.ROTATION_STEP_DEGREES / trackWidthPx
    }
    var isForeground by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    val isActive = isForeground && contentActive

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    isForeground = true
                }
                Lifecycle.Event.ON_STOP -> {
                    isForeground = false
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
     * Pasos de [activeIndex] al soltar.
     * Antes usaba [roundToInt] sobre el offset → hacía falta ~36° (~½ unidad) para avanzar;
     * un swipe medio quedaba pegado en la unidad actual.
     */
    fun dragStepsFromOffset(offsetDegrees: Float, velocityPxPerMs: Float = 0f): Int {
        val step = VitrinaConstants.ROTATION_STEP_DEGREES
        val indexSpaceDegrees = -offsetDegrees
        val threshold = step * VitrinaConstants.DRAG_SNAP_COMMIT_FRACTION
        val maxSteps = (itemCount - 1).coerceAtLeast(1)

        val flick =
            abs(velocityPxPerMs) >= VitrinaConstants.DRAG_FLICK_VELOCITY_PX_PER_MS &&
                abs(indexSpaceDegrees) >= threshold * 0.5f
        if (flick) {
            return (if (indexSpaceDegrees > 0) 1 else -1).coerceIn(-maxSteps, maxSteps)
        }

        if (abs(indexSpaceDegrees) < threshold) return 0

        val direction = if (indexSpaceDegrees > 0) 1 else -1
        val magnitude = (abs(indexSpaceDegrees) / step).toInt().coerceAtLeast(1)
        return (direction * magnitude).coerceIn(-maxSteps, maxSteps)
    }

    /** Cancela drag y restaura el cilindro a la unidad activa (p. ej. modal a mitad de gesto). */
    fun abortDragToCurrentUnit() {
        if (mode != VitrinaMode.Dragging) return
        dragAccumulatedX = 0f
        baseRotationHandle?.clearDragOffset()
        baseRotationHandle?.setBaseDegrees(displayRotationDegrees)
        mode = if (hasModalOpen) VitrinaMode.ModalOpen else VitrinaMode.Interactive
        idleClock.lastMs = System.currentTimeMillis()
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
        idleClock.lastMs = System.currentTimeMillis()
    }

    fun registerInteraction() {
        idleClock.lastMs = System.currentTimeMillis()
        if (!isUserActive) isUserActive = true
        if (!hasModalOpen && mode != VitrinaMode.Dragging && mode != VitrinaMode.Interactive) {
            mode = VitrinaMode.Interactive
        }
    }

    fun dismissScreenSaver() {
        idleClock.lastMs = System.currentTimeMillis()
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
                rotationAnimationSpec = when (source) {
                    "drag" -> VitrinaConstants.dragSnapAnimationSpec
                    else -> VitrinaConstants.manualRotationAnimationSpec
                }
                idleClock.lastMs = System.currentTimeMillis()
                isUserActive = true // corta idle continuo antes del snap
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
        idleClock.lastMs = System.currentTimeMillis()
        dragStartTimeMs = System.currentTimeMillis()
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
        val totalDragPx = dragAccumulatedX
        val offsetDegrees = dragVisualOffsetDegrees()
        val elapsedMs = (System.currentTimeMillis() - dragStartTimeMs).coerceAtLeast(1L)
        val velocityPxPerMs = totalDragPx / elapsedMs
        val steps = dragStepsFromOffset(offsetDegrees, velocityPxPerMs)
        dragAccumulatedX = 0f
        android.util.Log.d(
            "VitrinaGesture",
            "drag end offsetDeg=$offsetDegrees velocity=${"%.2f".format(velocityPxPerMs)} " +
                "steps=$steps activeIndex=$activeIndex",
        )
        if (steps == 0) {
            baseRotationHandle?.clearDragOffset()
            baseRotationHandle?.setBaseDegrees(displayRotationDegrees)
            settleInteractive()
        } else {
            baseRotationHandle?.commitDragIntoBase()
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

    LaunchedEffect(isActive) {
        if (isActive) {
            // Esperar a que cargue la escena: girar de entrada crasheaba Filament.
            delay(1_200L)
            if (!isActive) return@LaunchedEffect
            if (mode == VitrinaMode.Dragging || mode == VitrinaMode.ModalOpen) return@LaunchedEffect
            // Si el usuario tocó en esos 1.2s, no forzar idle.
            if (System.currentTimeMillis() - idleClock.lastMs < 1_000L) return@LaunchedEffect
            idleClock.lastMs = System.currentTimeMillis() - autoRotateAfterMs - 1_000L
            mode = VitrinaMode.AutoRotating
            isUserActive = false
        } else {
            // Fuera de Intro: parar idle/screensaver “invisible”.
            idleClock.lastMs = System.currentTimeMillis()
            if (mode == VitrinaMode.ScreenSaver || mode == VitrinaMode.AutoRotating) {
                mode = VitrinaMode.Interactive
            }
            isUserActive = true
        }
    }

    LaunchedEffect(itemCount, hasModalOpen, screenSaverEnabled, isActive) {
        while (true) {
            if (!isActive) {
                if (mode == VitrinaMode.ScreenSaver) {
                    mode = VitrinaMode.Interactive
                }
                // No acumular idle fuera de Intro.
                idleClock.lastMs = System.currentTimeMillis()
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

            val idleFor = now - idleClock.lastMs
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
