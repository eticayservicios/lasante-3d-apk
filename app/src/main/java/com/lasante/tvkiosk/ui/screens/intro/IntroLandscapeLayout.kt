package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import android.util.Log
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.data.VitrinaUnit
import com.lasante.tvkiosk.ui.utils.clickableWithSound

private const val PROFILE_LOG_TAG = "VitrinaProfile"

@Composable
fun IntroResponsiveLayout(
    widthClass: WindowWidthSizeClass,
    vitrinaUnits: List<VitrinaUnit>,
    activeVitrinaIndex: Int,
    vitrinaRotationDegrees: Float,
    vitrinaIsDragging: Boolean,
    baseRotationHandle: VitrinaBaseRotationHandle,
    vitrinaFilamentSession: VitrinaFilamentSession,
    showVitrinaProducts: Boolean,
    isUserActive: Boolean = true,
    renderVitrinaScene: Boolean = true,
    vitrinaSceneActive: Boolean = true,
    vitrinaFilamentRendering: Boolean = true,
    backdropBlurred: Boolean = false,
    vitrinaInteractionEnabled: Boolean = true,
    showVitrinaControls: Boolean = true,
    vitrinaRotationAnimationSpec: AnimationSpec<Float> = VitrinaConstants.manualRotationAnimationSpec,
    socialNetworks: List<SocialNetwork>,
    onProductClick: (Product) -> Unit,
    onWakeFromIdle: () -> Unit,
    onRotationAnimationFinished: () -> Unit = {},
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onVideoClick: () -> Unit,
    onRotateClick: () -> Unit,
    onNavigateToTreatments: (String) -> Unit,
    onSocialClick: (String, String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val metrics = introLayoutMetrics(widthClass)
        LaunchedEffect(
            metrics.vitrinaProfileKey,
            metrics.maxWidth,
            metrics.maxHeight,
            widthClass,
        ) {
            // Log siempre (también en tablet) para diagnosticar perfil activo.
            Log.i(
                PROFILE_LOG_TAG,
                "profile=${metrics.vitrinaProfileKey} " +
                    "preferTv66=${metrics.preferTv66} " +
                    "size=${metrics.maxWidth}x${metrics.maxHeight} " +
                    "widthClass=$widthClass " +
                    "isTablet=${metrics.isTabletLandscape} " +
                    "isPhoneLand=${metrics.isPhoneLandscape} " +
                    "isTv=${metrics.isTv} isTv42=${metrics.isTv42} isTv66=${metrics.isTv66} " +
                    "tv42Large=${metrics.isTv42LargeCanvas} " +
                    "manufacturer=${android.os.Build.MANUFACTURER} " +
                    "model=${android.os.Build.MODEL} " +
                    "badgeH=${metrics.bubblesBadgeHeight} badgeW=${metrics.bubblesBadgeWidthFraction} " +
                    "badgePullUp=${metrics.bubblesBadgeTopPullUp} " +
                    "bubbleSize=${metrics.bubbleSize} bubbleTop=${metrics.bubblesRowTopInScene} " +
                    "bubbleSpace=${metrics.bubbleSpacing} rowW=${metrics.bubblesRowWidthFraction} " +
                    "rotateEnd=${metrics.rotateButtonEndPadding} " +
                    "rotateX=${metrics.rotateButtonProtrudeOffset} " +
                    "vOffset=${metrics.vitrinaVerticalOffsetAdjustment} " +
                    "vBias=${metrics.vitrinaVerticalBias}",
            )
        }
        val vitrinaPadding = Modifier.padding(
            start = maxWidth * metrics.vitrinaInsetStartFraction,
            end = maxWidth * metrics.vitrinaInsetEndFraction,
            top = maxHeight * metrics.vitrinaInsetTopFraction,
            bottom = maxHeight * metrics.vitrinaInsetBottomFraction,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = metrics.horizontalPadding,
                    vertical = metrics.verticalPadding,
                ),
        ) {
            Box(modifier = Modifier.fillMaxSize().then(vitrinaPadding)) {
                BusinessUnitVitrina(
                    metrics = metrics,
                    vitrinaUnits = vitrinaUnits,
                    activeIndex = activeVitrinaIndex,
                    displayRotationDegrees = vitrinaRotationDegrees,
                    isDragging = vitrinaIsDragging,
                    baseRotationHandle = baseRotationHandle,
                    vitrinaFilamentSession = vitrinaFilamentSession,
                    showProducts = showVitrinaProducts,
                    isUserActive = isUserActive,
                    renderScene = renderVitrinaScene,
                    sceneActive = vitrinaSceneActive,
                    filamentRenderingEnabled = vitrinaFilamentRendering,
                    backdropBlurred = backdropBlurred,
                    vitrinaInteractionEnabled = vitrinaInteractionEnabled,
                    showOverlayControls = showVitrinaControls,
                    rotationAnimationSpec = vitrinaRotationAnimationSpec,
                    onProductClick = onProductClick,
                    onWakeFromIdle = onWakeFromIdle,
                    onRotationAnimationFinished = onRotationAnimationFinished,
                    onUnitClick = { unitId ->
                        onWakeFromIdle()
                        onNavigateToTreatments(unitId)
                    },
                    onDragStart = onDragStart,
                    onDrag = onDrag,
                    onDragEnd = onDragEnd,
                    onRotateClick = onRotateClick,
                )
            }

            // Redes sociales — centro izquierda (encima del SurfaceView de la vitrina)
            SocialRail(
                socialNetworks = socialNetworks,
                metrics = metrics,
                onSocialClick = onSocialClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = metrics.socialStartPadding)
                    .offset(y = metrics.socialCenterYOffset)
                    .zIndex(20f),
            )

            // Rail derecho: BadgeHistoria arriba; logo = altura de vitrina, centrado con ella.
            val logoH = metrics.vitrinaVisualHeight.coerceAtLeast(48.dp)
            val logoW = (logoH * (229f / 1004f)).coerceAtLeast(12.dp)
            val railWidth = maxOf(metrics.historiaBadgeWidth, logoW)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = metrics.logoEndPadding)
                    .offset(y = metrics.vitrinaCenterYOffset)
                    .width(railWidth)
                    .height(logoH)
                    .zIndex(15f),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = "file:///android_asset/vitrina/ui/logo_la_sante_vertical.png",
                    contentDescription = null,
                    modifier = Modifier
                        .height(logoH)
                        .width(logoW),
                    contentScale = ContentScale.Fit,
                )
            }
            if (showVitrinaControls) {
                val pullUp = metrics.historiaRailTopPullUp
                HistoriaBadgeButton(
                    metrics = metrics,
                    onClick = onVideoClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = metrics.logoEndPadding)
                        .offset(y = -pullUp)
                        .zIndex(16f),
                )
            }
        }
    }
}

@Composable
private fun HistoriaBadgeButton(
    metrics: IntroLayoutMetrics,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val gifModel = remember(context) {
        ImageRequest.Builder(context)
            .data("file:///android_asset/vitrina/ui/Historia.gif")
            .crossfade(false)
            .allowHardware(false)
            .build()
    }
    Box(
        modifier = modifier
            .width(metrics.historiaBadgeWidth)
            .height(metrics.historiaBadgeHeight)
            .clickableWithSound { onClick() },
        contentAlignment = Alignment.TopCenter,
    ) {
        AsyncImage(
            model = "file:///android_asset/vitrina/ui/badge_historia.png",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        AsyncImage(
            model = gifModel,
            contentDescription = null,
            modifier = Modifier
                .padding(top = metrics.historiaBadgeIconTop.coerceAtLeast(0.dp))
                .size(metrics.historiaBadgeIconSize),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
internal fun IntroActionButton(
    assetPath: String,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isGif = assetPath.endsWith(".gif", ignoreCase = true)
    val model = remember(assetPath, context, isGif) {
        ImageRequest.Builder(context)
            .data(assetPath)
            .crossfade(false)
            .apply {
                if (isGif) {
                    // Evita frames basura / flash blanco en GIFs con disposal en mid-range (Infinix).
                    allowHardware(false)
                }
            }
            .build()
    }
    AsyncImage(
        model = model,
        contentDescription = null,
        modifier = modifier
            .size(size)
            .clickableWithSound { onClick() },
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun SocialRail(
    socialNetworks: List<SocialNetwork>,
    metrics: IntroLayoutMetrics,
    onSocialClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(metrics.socialIconSpacing),
    ) {
        socialNetworks.forEach { social ->
            SocialNetworkIconButton(
                social = social,
                size = metrics.socialIconSize,
                onClick = { onSocialClick(social.label, social.url) },
            )
        }
    }
}
