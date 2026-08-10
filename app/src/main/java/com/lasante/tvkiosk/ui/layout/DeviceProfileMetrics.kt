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

/**
 * “Espacio” = avance del carácter U+0020 (barra espaciadora) en Poppins Regular
 * al tamaño del título principal Fire/TV42 (22.sp + 2 = 24.sp).
 * Medición TTF: 267/1000 em → 6.408.dp @ 24.sp.
 * Solo para offsets de Fire / Television_1080 (no Damasco / TV66 / Infinix).
 */
object FireTv42Spacing {
    val KeyboardSpace: Dp = 6.408.dp
    fun spaces(count: Int): Dp = KeyboardSpace * count
}

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
        preferTv66: Boolean = false,
    ): DeviceProfile {
        val isLandscape = maxWidth > maxHeight

        // TV66 Hikvision: canvas ref., preferTv66, o force DEBUG (Damasco).
        if (
            HikvisionLayoutDebug.isForced() ||
            Tv66Reference.matchesReferenceCanvas(maxWidth, maxHeight) ||
            preferTv66
        ) {
            android.util.Log.i(
                "Tv66Profile",
                "FORCE TV_LARGE canvas=${maxWidth.value.toInt()}×${maxHeight.value.toInt()} " +
                    "prefer=$preferTv66 hikForce=${HikvisionLayoutDebug.isForced()}",
            )
            return DeviceProfile(
                tier = DeviceProfileTier.TV_LARGE,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                isLandscape = isLandscape,
            )
        }

        val isPhoneLandscape = isLandscape && maxHeight < 520.dp &&
            !(maxWidth >= 640.dp && maxHeight >= 400.dp)
        val isTabletLandscape = isLandscape && maxWidth >= 640.dp && maxHeight >= 400.dp
        val isTv = maxWidth >= 880.dp && maxHeight >= 480.dp && !isPhoneLandscape
        val isTvLarge = isTv && maxWidth > Tv66Reference.MinWidthForLargeTier
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
        preferTv66: Boolean = false,
    ): SharedScreenMetrics {
        val profile = resolve(maxWidth, maxHeight, widthClass, preferTv66 = preferTv66)
        return SharedScreenMetrics(
            profile = profile,
            grid = gridMetrics(profile),
            nav = navMetrics(profile),
        )
    }

    private fun gridMetrics(profile: DeviceProfile): SharedGridMetrics {
        val w = profile.maxWidth
        // Base: 90% ancho útil (5% margen por lado). TV66: márgenes −10% y ancho útil −10%.
        val sideMargin = when (profile.tier) {
            DeviceProfileTier.TV_LARGE -> w * 0.05f * 0.90f
            else -> w * 0.05f
        }
        val maxContentWidth = when (profile.tier) {
            DeviceProfileTier.TV_LARGE -> w * 0.90f
            else -> w
        }
        return when (profile.tier) {
            DeviceProfileTier.COMPACT_LANDSCAPE -> SharedGridMetrics(
                // 5 cols (antes 6): cards/iconos más grandes en Infinix.
                columns = 5,
                horizontalPadding = sideMargin,
                maxContentWidth = maxContentWidth,
                contentPadding = 8.dp,
                cardSpacing = 8.dp,
                topPadding = 28.dp,
            )
            DeviceProfileTier.TV_LARGE -> SharedGridMetrics(
                // Hikvision 1280×720: 4 cols; top más bajo que canvas 4K altos.
                // cardSpacing: −10.dp vs 32 (margen lateral entre cards CT).
                columns = 4,
                horizontalPadding = sideMargin,
                maxContentWidth = maxContentWidth,
                contentPadding = 10.dp,
                cardSpacing = 22.dp,
                topPadding = if (profile.maxHeight <= Tv66Reference.Height + 40.dp) 28.dp else 58.dp,
            )
            DeviceProfileTier.TV_REGULAR -> SharedGridMetrics(
                // Fire / Ariana (~1137) y Damasco (~1333): 4 cols.
                columns = 4,
                horizontalPadding = sideMargin,
                maxContentWidth = maxContentWidth,
                contentPadding = 8.dp,
                cardSpacing = if (w >= 1200.dp) 12.dp else 14.dp,
                // Fire ~540H: 54.dp comía la 2.ª fila. Damasco canvas alto mantiene aire.
                topPadding = if (w >= 1200.dp) 54.dp else 28.dp,
            )
            DeviceProfileTier.TABLET_LANDSCAPE -> SharedGridMetrics(
                columns = if (profile.maxWidth >= 900.dp) 5 else 4,
                horizontalPadding = sideMargin,
                maxContentWidth = maxContentWidth,
                contentPadding = if (profile.isWide) 10.dp else 4.dp,
                cardSpacing = 14.dp,
                topPadding = 32.dp,
            )
            DeviceProfileTier.COMPACT_PORTRAIT -> SharedGridMetrics(
                columns = 2,
                horizontalPadding = sideMargin,
                maxContentWidth = maxContentWidth,
                contentPadding = 4.dp,
                cardSpacing = 12.dp,
                topPadding = 18.dp,
            )
            DeviceProfileTier.DEFAULT -> SharedGridMetrics(
                columns = if (profile.isLandscape && w >= 620.dp) 4 else 2,
                horizontalPadding = sideMargin,
                maxContentWidth = maxContentWidth,
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
            // Título más contenido en canvas bajo Hikvision (1280×720).
            buttonSize = 53.dp,
            buttonSpacing = 12.dp,
            titleFontSize = if (profile.maxHeight <= Tv66Reference.Height + 40.dp) 28.sp else 36.sp,
            titleUnderlineWidth = if (profile.maxHeight <= Tv66Reference.Height + 40.dp) 240.dp else 320.dp,
        )
        DeviceProfileTier.TV_REGULAR -> SharedNavMetrics(
            buttonSize = 38.dp,
            buttonSpacing = 12.dp,
            // −20% vs 28.sp: deja espacio para buscador + Back/Home en Productos.
            titleFontSize = 22.sp,
            titleUnderlineWidth = 176.dp,
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
