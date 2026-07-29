package com.lasante.tvkiosk.ui.layout

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Perfil responsive unificado para Intro, Tratamientos y Productos.
 * Tres familias principales: compact, tv-regular, tv-large (+ tablet landscape).
 */
enum class DeviceProfileTier {
    COMPACT_LANDSCAPE,
    COMPACT_PORTRAIT,
    TABLET_LANDSCAPE,
    TV_REGULAR,
    TV_LARGE,
    DEFAULT,
}

@Immutable
data class DeviceProfile(
    val tier: DeviceProfileTier,
    val maxWidth: Dp,
    val maxHeight: Dp,
    val isLandscape: Boolean,
) {
    val isCompactLandscape: Boolean get() = tier == DeviceProfileTier.COMPACT_LANDSCAPE
    val isTvRegular: Boolean get() = tier == DeviceProfileTier.TV_REGULAR
    val isTvLarge: Boolean get() = tier == DeviceProfileTier.TV_LARGE
    val isWide: Boolean get() = maxWidth >= 900.dp
}

@Immutable
data class SharedGridMetrics(
    val columns: Int,
    val horizontalPadding: Dp,
    val maxContentWidth: Dp,
    val contentPadding: Dp,
    val cardSpacing: Dp,
    val topPadding: Dp,
)

@Immutable
data class SharedNavMetrics(
    val buttonSize: Dp,
    val buttonSpacing: Dp,
    val titleFontSize: TextUnit,
    val titleUnderlineWidth: Dp,
)

@Immutable
data class SharedScreenMetrics(
    val profile: DeviceProfile,
    val grid: SharedGridMetrics,
    val nav: SharedNavMetrics,
)

object DeviceProfileResolver {
    fun resolve(
        maxWidth: Dp,
        maxHeight: Dp,
        widthClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    ): DeviceProfile {
        val isLandscape = maxWidth > maxHeight
        val isPhoneLandscape = isLandscape && maxHeight < 520.dp &&
            !(maxWidth >= 640.dp && maxHeight >= 400.dp)
        val isTabletLandscape = isLandscape && maxWidth >= 640.dp && maxHeight >= 400.dp
        val isTv = maxWidth >= 880.dp && maxHeight >= 480.dp && !isPhoneLandscape
        val isTvLarge = isTv && maxWidth > 1400.dp
        val isTvRegular = isTv && !isTvLarge

        val tier = when {
            isTvLarge -> DeviceProfileTier.TV_LARGE
            isTvRegular -> DeviceProfileTier.TV_REGULAR
            isPhoneLandscape -> DeviceProfileTier.COMPACT_LANDSCAPE
            isTabletLandscape -> DeviceProfileTier.TABLET_LANDSCAPE
            isLandscape && maxWidth >= 700.dp -> DeviceProfileTier.TABLET_LANDSCAPE
            !isLandscape && maxWidth < 420.dp -> DeviceProfileTier.COMPACT_PORTRAIT
            widthClass == WindowWidthSizeClass.Expanded -> DeviceProfileTier.TABLET_LANDSCAPE
            else -> DeviceProfileTier.DEFAULT
        }

        return DeviceProfile(
            tier = tier,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            isLandscape = isLandscape,
        )
    }

    fun screenMetrics(
        maxWidth: Dp,
        maxHeight: Dp,
        widthClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    ): SharedScreenMetrics {
        val profile = resolve(maxWidth, maxHeight, widthClass)
        return SharedScreenMetrics(
            profile = profile,
            grid = gridMetrics(profile),
            nav = navMetrics(profile),
        )
    }

    private fun gridMetrics(profile: DeviceProfile): SharedGridMetrics {
        val w = profile.maxWidth
        return when (profile.tier) {
            DeviceProfileTier.COMPACT_LANDSCAPE -> SharedGridMetrics(
                columns = 6,
                horizontalPadding = 16.dp,
                maxContentWidth = 702.dp,
                contentPadding = 4.dp,
                cardSpacing = 10.dp,
                topPadding = 28.dp,
            )
            DeviceProfileTier.TV_LARGE -> SharedGridMetrics(
                columns = 4,
                horizontalPadding = 42.dp,
                maxContentWidth = 1332.dp,
                contentPadding = 10.dp,
                cardSpacing = 24.dp,
                topPadding = 58.dp,
            )
            DeviceProfileTier.TV_REGULAR -> SharedGridMetrics(
                columns = 5,
                horizontalPadding = 24.dp,
                maxContentWidth = 828.dp,
                contentPadding = 10.dp,
                cardSpacing = 14.dp,
                topPadding = 54.dp,
            )
            DeviceProfileTier.TABLET_LANDSCAPE -> SharedGridMetrics(
                columns = if (profile.maxWidth >= 900.dp) 5 else 4,
                horizontalPadding = if (w >= 1200.dp) 42.dp else if (w >= 800.dp) 20.dp else 16.dp,
                maxContentWidth = if (profile.isWide) 1008.dp else 648.dp,
                contentPadding = if (profile.isWide) 10.dp else 4.dp,
                cardSpacing = 14.dp,
                topPadding = 32.dp,
            )
            DeviceProfileTier.COMPACT_PORTRAIT -> SharedGridMetrics(
                columns = 2,
                horizontalPadding = 16.dp,
                maxContentWidth = 648.dp,
                contentPadding = 4.dp,
                cardSpacing = 12.dp,
                topPadding = 18.dp,
            )
            DeviceProfileTier.DEFAULT -> SharedGridMetrics(
                columns = if (profile.isLandscape && w >= 620.dp) 4 else 2,
                horizontalPadding = 16.dp,
                maxContentWidth = 648.dp,
                contentPadding = 4.dp,
                cardSpacing = 12.dp,
                topPadding = if (profile.isLandscape) 32.dp else 18.dp,
            )
        }
    }

    private fun navMetrics(profile: DeviceProfile): SharedNavMetrics = when (profile.tier) {
        DeviceProfileTier.COMPACT_LANDSCAPE -> SharedNavMetrics(
            buttonSize = 32.dp,
            buttonSpacing = 10.dp,
            titleFontSize = 20.sp,
            titleUnderlineWidth = 150.dp,
        )
        DeviceProfileTier.TV_LARGE -> SharedNavMetrics(
            buttonSize = 53.dp,
            buttonSpacing = 12.dp,
            titleFontSize = 36.sp,
            titleUnderlineWidth = 320.dp,
        )
        DeviceProfileTier.TV_REGULAR -> SharedNavMetrics(
            buttonSize = 38.dp,
            buttonSpacing = 12.dp,
            titleFontSize = 28.sp,
            titleUnderlineWidth = 220.dp,
        )
        DeviceProfileTier.TABLET_LANDSCAPE, DeviceProfileTier.DEFAULT -> SharedNavMetrics(
            buttonSize = if (profile.isWide) 40.dp else 35.dp,
            buttonSpacing = 12.dp,
            titleFontSize = if (profile.isWide) 30.sp else 22.sp,
            titleUnderlineWidth = if (profile.isWide) 250.dp else 170.dp,
        )
        DeviceProfileTier.COMPACT_PORTRAIT -> SharedNavMetrics(
            buttonSize = 35.dp,
            buttonSpacing = 12.dp,
            titleFontSize = 22.sp,
            titleUnderlineWidth = 170.dp,
        )
    }

    fun modalMetrics(profile: DeviceProfile): SharedModalMetrics = when (profile.tier) {
        DeviceProfileTier.COMPACT_LANDSCAPE -> SharedModalMetrics(
            modalWidthFraction = 0.90f,
            modalHeightFraction = 0.68f,
            modelWeight = 1.55f,
            descriptionWeight = 0.82f,
            descriptionHeightFraction = 0.96f,
            columnSpacing = 10.dp,
            rowOffsetX = (-20).dp,
            rowOffsetY = (-12).dp,
            descriptionOffsetY = 0.dp,
            descriptionHorizontalPadding = 20.dp,
            modelScaleToUnits = 1.656f,
            descriptionAlignTop = true,
            alignRowTop = true,
        )
        DeviceProfileTier.TV_LARGE -> SharedModalMetrics(
            modalWidthFraction = 0.86f,
            modalHeightFraction = 0.64f,
            modelWeight = 1.55f,
            descriptionWeight = 0.82f,
            descriptionHeightFraction = 0.86f,
            columnSpacing = 18.dp,
            rowOffsetX = (-44).dp,
            rowOffsetY = (-26).dp,
            descriptionOffsetY = (-22).dp,
            descriptionHorizontalPadding = 32.dp,
            modelScaleToUnits = 1.72f,
            descriptionAlignTop = false,
            alignRowTop = false,
        )
        DeviceProfileTier.TV_REGULAR -> SharedModalMetrics(
            modalWidthFraction = 0.84f,
            modalHeightFraction = 0.62f,
            modelWeight = 1.55f,
            descriptionWeight = 0.82f,
            descriptionHeightFraction = 0.84f,
            columnSpacing = 16.dp,
            rowOffsetX = (-40).dp,
            rowOffsetY = (-24).dp,
            descriptionOffsetY = (-20).dp,
            descriptionHorizontalPadding = 30.dp,
            modelScaleToUnits = 1.656f,
            descriptionAlignTop = false,
            alignRowTop = false,
        )
        else -> SharedModalMetrics(
            modalWidthFraction = 0.84f,
            modalHeightFraction = 0.62f,
            modelWeight = 1.50f,
            descriptionWeight = 0.88f,
            descriptionHeightFraction = 0.80f,
            columnSpacing = 14.dp,
            rowOffsetX = (-36).dp,
            rowOffsetY = (-22).dp,
            descriptionOffsetY = (-18).dp,
            descriptionHorizontalPadding = 30.dp,
            modelScaleToUnits = 1.656f,
            descriptionAlignTop = false,
            alignRowTop = false,
        )
    }
}

@Immutable
data class SharedModalMetrics(
    val modalWidthFraction: Float,
    val modalHeightFraction: Float,
    val modelWeight: Float,
    val descriptionWeight: Float,
    val descriptionHeightFraction: Float,
    val columnSpacing: Dp,
    val rowOffsetX: Dp,
    val rowOffsetY: Dp,
    val descriptionOffsetY: Dp,
    val descriptionHorizontalPadding: Dp,
    val modelScaleToUnits: Float,
    val descriptionAlignTop: Boolean,
    val alignRowTop: Boolean,
)
