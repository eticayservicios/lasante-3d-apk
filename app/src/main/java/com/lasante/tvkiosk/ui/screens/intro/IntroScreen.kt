package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.imageLoader
import com.lasante.tvkiosk.ui.utils.ModalFrostScrim
import com.lasante.tvkiosk.ui.utils.ModalOverlayDialog
import com.lasante.tvkiosk.ui.utils.modalBackdropBlur
import com.lasante.tvkiosk.ui.utils.needsLegacyModalBlurFallback
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.lasante.tvkiosk.data.BusinessUnit
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.data.ScreenSaverVideo
import com.lasante.tvkiosk.data.VitrinaConfig
import com.lasante.tvkiosk.data.VitrinaUnit
import com.lasante.tvkiosk.ui.theme.LaSanteBackground

@Composable
fun IntroScreen(
    businessUnits: List<BusinessUnit>,
    vitrinaUnits: List<VitrinaUnit>,
    vitrinaConfig: VitrinaConfig,
    screenSaverVideos: List<ScreenSaverVideo>,
    institutionalVideoUrl: String?,
    windowSizeClass: WindowSizeClass,
    contentActive: Boolean = true,
    onStopAudio: () -> Unit,
    onNavigateToTreatments: (String) -> Unit = {},
) {
    val widthClass = windowSizeClass.widthSizeClass
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val isTabletLandscape = configuration.screenWidthDp > configuration.screenHeightDp &&
        configuration.screenWidthDp >= 640 &&
        configuration.screenHeightDp >= 400

    // Precarga GIFs de Intro una vez: al volver ya están en cache Coil.
    LaunchedEffect(Unit) {
        listOf(
            VitrinaUiImages.HISTORIA_GIF,
            VitrinaUiImages.GIRA_GIF,
            VitrinaUiImages.TOUCH_GIF,
        ).forEach { path ->
            context.imageLoader.enqueue(VitrinaUiImages.request(context, path))
        }
    }

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var isVideoPlaying by remember { mutableStateOf(false) }
    var qrUrl by remember { mutableStateOf<String?>(null) }
    var qrLabel by remember { mutableStateOf("") }

    val socialNetworks = remember { SocialNetworks.defaults }

    // Nuestra Historia: Video 1 de Admin > Vitrina > Videos; fallback a videoInstitucional legacy.
    val historiaVideoUrl = resolveHistoriaVideoUrl(
        institutionalVideoUrl = institutionalVideoUrl,
        screenSaverVideos = screenSaverVideos,
    )

    val displayVitrinaUnits = rememberDisplayVitrinaUnits(vitrinaUnits)
    // Producto 3D también es “heavy”: pausa Filament de la vitrina (queda el último frame)
    // y el blur Compose sigue viéndose detrás del modal — evita 2 engines a la vez (OOM Redmi).
    val hasHeavyModalOpen = selectedProduct != null || isVideoPlaying || qrUrl != null
    val hasModalOpen = hasHeavyModalOpen
    val lifecycleOwner = LocalLifecycleOwner.current
    val baseRotationHandle = remember { VitrinaBaseRotationHandle() }
    val vitrinaFilamentSession = rememberVitrinaFilamentSession()

    var appInForeground by remember { mutableStateOf(true) }
    // Prefetch sigue en CT/Productos (Intro montada, Filament pausado). Solo pausa en background.
    IntroDeferredVideoPrefetch(
        vitrinaFilamentSession = vitrinaFilamentSession,
        screenSaverVideos = screenSaverVideos,
        institutionalVideoUrl = institutionalVideoUrl,
        enabled = appInForeground,
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> appInForeground = true
                Lifecycle.Event.ON_STOP -> {
                    appInForeground = false
                    isVideoPlaying = false
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Al salir de Intro (p. ej. navegar a tratamientos), limpiar modales para no restaurar estado 3D
    DisposableEffect(Unit) {
        onDispose {
            selectedProduct = null
            isVideoPlaying = false
            qrUrl = null
        }
    }

    val vitrinaController = rememberVitrinaInteractionController(
        itemCount = VitrinaConstants.UNIT_COUNT,
        hasModalOpen = hasModalOpen,
        baseRotationHandle = baseRotationHandle,
        contentActive = contentActive,
        screenSaverEnabled = vitrinaConfig.screenSaverPlaylistEnabled && screenSaverVideos.isNotEmpty(),
        autoRotateAfterMs = vitrinaConfig.autoRotateAfterMs,
        screenSaverAfterMs = vitrinaConfig.screenSaverAfterMs,
    )

    val shouldPlayScreenSaver = contentActive &&
        vitrinaController.isScreenSaverActive &&
        screenSaverVideos.isNotEmpty() &&
        !hasModalOpen

    // Mismo comportamiento en todos los perfiles: vitrina montada, render pausado, video encima.
    val renderVitrinaScene = contentActive

    // Con modal de producto: render OFF (frame congelado) + blur ON → mismo look, menos RAM/GPU.
    val vitrinaFilamentRendering = contentActive && !shouldPlayScreenSaver && !hasHeavyModalOpen
    val vitrinaSceneActive = contentActive && !hasModalOpen && !shouldPlayScreenSaver
    val vitrinaInteractionEnabled = contentActive && !hasModalOpen
    val showVitrinaControls = contentActive && !shouldPlayScreenSaver && !hasModalOpen
    val productModalBlur = selectedProduct != null
    val legacyTabletModalBlur = productModalBlur &&
        isTabletLandscape &&
        needsLegacyModalBlurFallback()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LaSanteBackground)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    // Registrar interacción para detener el giro automático inmediato y encender la vitrina activa actual.
                    // No seleccionamos una unidad al azar al tocar cualquier parte de la pantalla.
                    vitrinaController.registerInteraction()
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .modalBackdropBlur(productModalBlur),
        ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val path1 = Path().apply {
                    moveTo(width * 0.65f, 0f)
                    quadraticBezierTo(width * 0.8f, height * 0.25f, width, height * 0.45f)
                    lineTo(width, 0f)
                    close()
                }
                drawPath(path1, color = Color.White.copy(alpha = 0.5f))
                val path2 = Path().apply {
                    moveTo(0f, height * 0.7f)
                    quadraticBezierTo(width * 0.2f, height * 0.85f, width * 0.4f, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(path2, color = Color.White.copy(alpha = 0.3f))
            }

            IntroResponsiveLayout(
                widthClass = widthClass,
                vitrinaUnits = displayVitrinaUnits,
                activeVitrinaIndex = vitrinaController.activeIndex,
                vitrinaRotationDegrees = vitrinaController.displayRotationDegrees,
                vitrinaIsDragging = vitrinaController.isDragging,
                baseRotationHandle = baseRotationHandle,
                vitrinaFilamentSession = vitrinaFilamentSession,
                showVitrinaProducts = vitrinaController.showProducts,
                isUserActive = vitrinaController.isUserActive,
                renderVitrinaScene = renderVitrinaScene,
                vitrinaSceneActive = vitrinaSceneActive,
                vitrinaFilamentRendering = vitrinaFilamentRendering,
                vitrinaInteractionEnabled = vitrinaInteractionEnabled,
                showVitrinaControls = showVitrinaControls,
                vitrinaRotationAnimationSpec = vitrinaController.rotationAnimationSpec,
                backdropBlurred = productModalBlur,
                socialNetworks = socialNetworks,
                onProductClick = {
                    vitrinaController.registerInteraction()
                    if (it.isVitrinaModalEnabled()) {
                        selectedProduct = it
                    }
                },
                onSocialClick = { label, url ->
                    vitrinaController.registerInteraction()
                    qrLabel = label
                    qrUrl = url
                },
                onVideoClick = {
                    vitrinaController.registerInteraction()
                    isVideoPlaying = true
                },
                onRotateClick = { vitrinaController.rotateOnce() },
                onNavigateToTreatments = { unitId ->
                    vitrinaController.registerInteraction()
                    onNavigateToTreatments(unitId)
                },
                onWakeFromIdle = { vitrinaController.registerInteraction() },
                onRotationAnimationFinished = { vitrinaController.onRotationAnimationFinished() },
                onDragStart = { vitrinaController.onDragStart() },
                onDrag = { delta -> vitrinaController.onDrag(delta) },
                onDragEnd = { vitrinaController.onDragEnd() }
            )
        }

        ModalFrostScrim(visible = productModalBlur, strong = legacyTabletModalBlur)

        if (selectedProduct != null) {
            IntroProductDetailModal(
                product = selectedProduct!!,
                widthClass = widthClass,
                onStopAudio = onStopAudio,
                onClose = { selectedProduct = null },
            )
        }

        if (isVideoPlaying) {
            ModalOverlayDialog(onDismiss = { isVideoPlaying = false }) {
                VideoPlayerModal(
                    videoUrl = historiaVideoUrl,
                    onClose = { isVideoPlaying = false },
                    widthClass = widthClass,
                )
            }
        }

        if (qrUrl != null) {
            ModalOverlayDialog(onDismiss = { qrUrl = null }) {
                IntroSocialQrModal(
                    label = qrLabel,
                    url = qrUrl!!,
                    onClose = { qrUrl = null },
                )
            }
        }

        if (shouldPlayScreenSaver) {
            ScreenSaverVideoOverlay(
                videos = screenSaverVideos,
                onClose = { vitrinaController.dismissScreenSaver() }
            )
        }
    }
}

/** Video 1 de la playlist de vitrina (order ascendente); si no hay, `videoInstitucional` del home. */
private fun resolveHistoriaVideoUrl(
    institutionalVideoUrl: String?,
    screenSaverVideos: List<ScreenSaverVideo>,
): String? {
    val adminVideo1 = screenSaverVideos
        .sortedBy { it.order }
        .firstOrNull { it.enabled && it.url.isNotBlank() }
        ?.url
        ?.trim()
        ?.takeIf { it.isNotBlank() }
    return adminVideo1 ?: institutionalVideoUrl?.trim()?.takeIf { it.isNotBlank() }
}
