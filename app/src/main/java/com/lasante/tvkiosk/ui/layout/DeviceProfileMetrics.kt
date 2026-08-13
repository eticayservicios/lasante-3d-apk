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
 * Unidad de offset en métricas TV42 / headers de catálogo.
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
            if (com.lasante.tvkiosk.BuildConfig.DEBUG) {
                android.util.Log.i(
                    "Tv66Profile",
                    "FORCE TV_LARGE canvas=${maxWidth.value.toInt()}×${maxHeight.value.toInt()} " +
                        "prefer=$preferTv66 hikForce=${HikvisionLayoutDebug.isForced()}",
                )
            }
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
                // cardSpacing: aire entre cards CT/Productos (un poco más pegados, no juntos).
                columns = 4,
                horizontalPadding = sideMargin,
                maxContentWidth = maxContentWidth,
                contentPadding = 10.dp,
                cardSpacing = 16.dp,
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
            // Hikvision 720: 28.sp. Damasco/canvas altos: 36×0.80 (−20%).
            buttonSize = 53.dp,
            buttonSpacing = 12.dp,
            titleFontSize = if (profile.maxHeight <= Tv66Reference.Height + 40.dp) {
                28.sp
            } else {
                36.sp * 0.80f
            },
            titleUnderlineWidth = if (profile.maxHeight <= Tv66Reference.Height + 40.dp) {
                240.dp
            } else {
                320.dp * 0.80f
            },
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

    fun modalMetrics(profile: DeviceProfile): SharedModalMetrics {
        val w = profile.maxWidth
        return when (profile.tier) {
            /**
             * Hikvision TV66 1280×720 — referencia visual validada.
             * Gaps en “espacios” Poppins (misma unidad usada en el ajuste en tablet forzada).
             */
            DeviceProfileTier.TV_LARGE -> SharedModalMetrics(
                // +5% ancho vs 0.70: más aire para la columna GLB sin achicar el modelo.
                modalWidthFraction = 0.735f,
                modalHeightFraction = 0.68f,
                modelWeight = 1.25f,
                descriptionWeight = 1.40f,
                descriptionHeightFraction = 0.92f,
                cardHeightFraction = 0.80f,
                columnSpacing = FireTv42Spacing.spaces(10),
                cardWidthTrim = FireTv42Spacing.spaces(8),
                closeSideGap = FireTv42Spacing.spaces(2),
                rowOffsetX = (-8).dp,
                rowOffsetY = (-8).dp,
                descriptionOffsetX = 0.dp,
                descriptionOffsetY = FireTv42Spacing.spaces(1),
                descriptionHorizontalPadding = 30.dp,
                // Base TV66 × +3% zoom GLB.
                modelScaleToUnits = 1.72f * 1.03f,
                descriptionAlignTop = true,
                alignRowTop = false,
            )

            /**
             * Fire / Television_1080 (TV_REGULAR).
             * Misma intención de diseño; % un poco más alto (canvas distinto) y
             * gaps en [FireTv42Spacing] (unidad nativa de este perfil).
             */
            DeviceProfileTier.TV_REGULAR -> SharedModalMetrics(
                // +5% ancho / +0.10 peso GLB (misma proporción que TV66).
                modalWidthFraction = 0.777f,
                modalHeightFraction = 0.66f,
                modelWeight = 1.30f,
                descriptionWeight = 1.30f,
                descriptionHeightFraction = 0.90f,
                cardHeightFraction = 0.82f,
                columnSpacing = FireTv42Spacing.spaces(10),
                cardWidthTrim = FireTv42Spacing.spaces(8),
                closeSideGap = FireTv42Spacing.spaces(2),
                rowOffsetX = (-8).dp,
                rowOffsetY = (-8).dp,
                descriptionOffsetX = 0.dp,
                descriptionOffsetY = FireTv42Spacing.spaces(1),
                descriptionHorizontalPadding = 28.dp,
                modelScaleToUnits = 1.656f * 1.03f,
                descriptionAlignTop = true,
                alignRowTop = false,
            )

            /**
             * Tablets (Damasco / Infinix / landscape): gaps relativos al ancho,
             * no [FireTv42Spacing] (comentario histórico: no Damasco/Infinix).
             * Más % de modal porque el canvas físico es más chico.
             */
            DeviceProfileTier.TABLET_LANDSCAPE -> SharedModalMetrics(
                modalWidthFraction = 0.819f,
                modalHeightFraction = 0.70f,
                modelWeight = 1.30f,
                descriptionWeight = 1.25f,
                descriptionHeightFraction = 0.90f,
                cardHeightFraction = 0.82f,
                columnSpacing = (w * 0.05f).coerceIn(28.dp, 64.dp),
                cardWidthTrim = (w * 0.04f).coerceIn(20.dp, 52.dp),
                closeSideGap = (w * 0.012f).coerceIn(8.dp, 16.dp),
                rowOffsetX = (-6).dp,
                rowOffsetY = (-6).dp,
                descriptionOffsetX = 0.dp,
                descriptionOffsetY = (w * 0.006f).coerceIn(4.dp, 10.dp),
                descriptionHorizontalPadding = 24.dp,
                modelScaleToUnits = 1.55f * 1.02f,
                descriptionAlignTop = true,
                alignRowTop = false,
            )

            /** Phone landscape: suele ir apilado (compact); métricas por si usa fila. */
            DeviceProfileTier.COMPACT_LANDSCAPE -> SharedModalMetrics(
                modalWidthFraction = 0.945f,
                modalHeightFraction = 0.78f,
                modelWeight = 1.35f,
                descriptionWeight = 1.10f,
                descriptionHeightFraction = 0.96f,
                cardHeightFraction = 0.88f,
                columnSpacing = 12.dp,
                cardWidthTrim = 8.dp,
                closeSideGap = 8.dp,
                rowOffsetX = (-12).dp,
                rowOffsetY = (-8).dp,
                descriptionOffsetX = 0.dp,
                descriptionOffsetY = 0.dp,
                descriptionHorizontalPadding = 18.dp,
                modelScaleToUnits = 1.45f * 1.02f,
                descriptionAlignTop = true,
                alignRowTop = true,
            )

            DeviceProfileTier.COMPACT_PORTRAIT -> SharedModalMetrics(
                modalWidthFraction = 0.987f,
                modalHeightFraction = 0.82f,
                modelWeight = 1.30f,
                descriptionWeight = 1.10f,
                descriptionHeightFraction = 0.96f,
                cardHeightFraction = 0.90f,
                columnSpacing = 10.dp,
                cardWidthTrim = 6.dp,
                closeSideGap = 8.dp,
                rowOffsetX = 0.dp,
                rowOffsetY = 0.dp,
                descriptionOffsetX = 0.dp,
                descriptionOffsetY = 0.dp,
                descriptionHorizontalPadding = 16.dp,
                modelScaleToUnits = 1.38f,
                descriptionAlignTop = true,
                alignRowTop = true,
            )

            /** Default / fallback: proporciones tipo tablet. */
            else -> SharedModalMetrics(
                modalWidthFraction = 0.798f,
                modalHeightFraction = 0.68f,
                modelWeight = 1.25f,
                descriptionWeight = 1.25f,
                descriptionHeightFraction = 0.88f,
                cardHeightFraction = 0.82f,
                columnSpacing = (w * 0.045f).coerceIn(24.dp, 56.dp),
                cardWidthTrim = (w * 0.035f).coerceIn(16.dp, 48.dp),
                closeSideGap = (w * 0.012f).coerceIn(8.dp, 14.dp),
                rowOffsetX = (-6).dp,
                rowOffsetY = (-6).dp,
                descriptionOffsetX = 0.dp,
                descriptionOffsetY = 6.dp,
                descriptionHorizontalPadding = 24.dp,
                modelScaleToUnits = 1.50f * 1.02f,
                descriptionAlignTop = true,
                alignRowTop = false,
            )
        }
    }

    /**
     * Modal de filtros CT (presentación + estrellas).
     * Baseline visual = [DeviceProfileTier.TV_LARGE] (Hikvision 1280×720 validado).
     */
    fun filterSheetMetrics(profile: DeviceProfile): FilterSheetMetrics {
        return when (profile.tier) {
            DeviceProfileTier.TV_LARGE -> FilterSheetMetrics(
                widthFraction = 0.53f,
                heightFraction = 0.60f,
                maxWidth = 700.dp,
                paddingH = 66.dp,
                paddingTop = 45.dp,
                paddingBottom = 36.dp,
                titleSp = 28,
                subtitleSp = 15,
                helpSp = 12,
                helpLineSp = 15,
                optionSp = 13,
                buttonSp = 14,
                selectSp = 14,
                titleUnderlineWidth = 72.dp,
                closeSize = 40.dp,
                selectHeight = 46.dp,
                actionWidth = 168.dp,
                actionHeight = 44.dp,
                actionFillColumn = false,
                menuHeight = 108.dp,
                columnGap = 22.dp,
                checkboxSize = 22.dp,
                checkboxIconSize = 16.dp,
            )

            DeviceProfileTier.TV_REGULAR -> FilterSheetMetrics(
                widthFraction = 0.56f,
                heightFraction = 0.62f,
                maxWidth = 720.dp,
                paddingH = 56.dp,
                paddingTop = 36.dp,
                paddingBottom = 30.dp,
                titleSp = 26,
                subtitleSp = 14,
                helpSp = 12,
                helpLineSp = 15,
                optionSp = 13,
                buttonSp = 14,
                selectSp = 14,
                titleUnderlineWidth = 68.dp,
                closeSize = 38.dp,
                selectHeight = 44.dp,
                actionWidth = 160.dp,
                actionHeight = 42.dp,
                actionFillColumn = false,
                menuHeight = 104.dp,
                columnGap = 18.dp,
                checkboxSize = 22.dp,
                checkboxIconSize = 16.dp,
            )

            DeviceProfileTier.TABLET_LANDSCAPE,
            DeviceProfileTier.DEFAULT,
            -> FilterSheetMetrics(
                widthFraction = 0.62f,
                // Damasco: −0,5 cm ≈ −0.04 de alto de pantalla
                heightFraction = 0.62f,
                maxWidth = 640.dp,
                paddingH = 40.dp,
                paddingTop = 28.dp,
                paddingBottom = 24.dp,
                titleSp = 24,
                subtitleSp = 14,
                helpSp = 11,
                helpLineSp = 14,
                optionSp = 12,
                buttonSp = 13,
                selectSp = 13,
                titleUnderlineWidth = 64.dp,
                closeSize = 36.dp,
                selectHeight = 42.dp,
                actionWidth = 148.dp,
                actionHeight = 40.dp,
                actionFillColumn = false,
                menuHeight = 100.dp,
                columnGap = 14.dp,
                checkboxSize = 20.dp,
                checkboxIconSize = 14.dp,
            )

            DeviceProfileTier.COMPACT_LANDSCAPE -> FilterSheetMetrics(
                // Infinix / phone landscape: más angosto, un poco más alto, UI más chica.
                widthFraction = 0.80f,
                heightFraction = 0.80f,
                maxWidth = 440.dp,
                paddingH = 14.dp,
                paddingTop = 12.dp,
                paddingBottom = 12.dp,
                titleSp = 18,
                subtitleSp = 11,
                helpSp = 10,
                helpLineSp = 12,
                optionSp = 11,
                buttonSp = 11,
                selectSp = 11,
                titleUnderlineWidth = 48.dp,
                closeSize = 28.dp,
                selectHeight = 32.dp,
                actionWidth = 120.dp,
                actionHeight = 32.dp,
                actionFillColumn = true,
                menuHeight = 100.dp,
                columnGap = 8.dp,
                checkboxSize = 18.dp,
                checkboxIconSize = 12.dp,
            )

            DeviceProfileTier.COMPACT_PORTRAIT -> FilterSheetMetrics(
                widthFraction = 0.96f,
                heightFraction = 0.78f,
                maxWidth = 520.dp,
                paddingH = 20.dp,
                paddingTop = 16.dp,
                paddingBottom = 16.dp,
                titleSp = 22,
                subtitleSp = 13,
                helpSp = 11,
                helpLineSp = 14,
                optionSp = 12,
                buttonSp = 13,
                selectSp = 13,
                titleUnderlineWidth = 56.dp,
                closeSize = 32.dp,
                selectHeight = 40.dp,
                actionWidth = 140.dp,
                actionHeight = 40.dp,
                actionFillColumn = true,
                menuHeight = 96.dp,
                columnGap = 10.dp,
                checkboxSize = 20.dp,
                checkboxIconSize = 14.dp,
            )
        }
    }
}

@Immutable
data class SharedModalMetrics(
    val modalWidthFraction: Float,
    val modalHeightFraction: Float,
    val modelWeight: Float,
    val descriptionWeight: Float,
    val descriptionHeightFraction: Float,
    /** Alto del card sólido dentro de su columna (0–1). */
    val cardHeightFraction: Float = 0.80f,
    val columnSpacing: Dp,
    /** Recorte de ancho del bloque card+close (equiv. “−N espacios” en TV). */
    val cardWidthTrim: Dp = 0.dp,
    /** Separación horizontal entre card y botón close. */
    val closeSideGap: Dp = 8.dp,
    val rowOffsetX: Dp,
    val rowOffsetY: Dp,
    val descriptionOffsetX: Dp = 0.dp,
    val descriptionOffsetY: Dp,
    val descriptionHorizontalPadding: Dp,
    val modelScaleToUnits: Float,
    val descriptionAlignTop: Boolean,
    val alignRowTop: Boolean,
)

/** Métricas del modal de filtros de clase terapéutica. */
@Immutable
data class FilterSheetMetrics(
    val widthFraction: Float,
    val heightFraction: Float,
    val maxWidth: Dp,
    val paddingH: Dp,
    val paddingTop: Dp,
    val paddingBottom: Dp,
    val titleSp: Int,
    val subtitleSp: Int,
    val helpSp: Int,
    val helpLineSp: Int,
    val optionSp: Int,
    val buttonSp: Int,
    val selectSp: Int,
    val titleUnderlineWidth: Dp,
    val closeSize: Dp,
    val selectHeight: Dp,
    val actionWidth: Dp,
    val actionHeight: Dp,
    /** En compact: el botón de acción usa casi todo el ancho de la columna. */
    val actionFillColumn: Boolean,
    val menuHeight: Dp,
    val columnGap: Dp,
    val checkboxSize: Dp,
    val checkboxIconSize: Dp,
)
