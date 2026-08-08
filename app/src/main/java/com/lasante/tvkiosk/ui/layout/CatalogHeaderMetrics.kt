package com.lasante.tvkiosk.ui.layout

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasante.tvkiosk.ui.components.LaSanteScreenTitle
import com.lasante.tvkiosk.ui.screens.treatments.TreatmentUiMetrics
import com.lasante.tvkiosk.ui.theme.LaSanteText
import kotlin.math.roundToInt

/**
 * Layout de catálogo (CT + Productos): una sola resolución por canvas.
 *
 * 1 “espacio” = [FireTv42Spacing.KeyboardSpace] ≈ 6.408.dp.
 */
@Immutable
data class CatalogLayout(
    val profile: DeviceProfile,
    val grid: SharedGridMetrics,
    val nav: SharedNavMetrics,
    val ui: TreatmentUiMetrics.ProfileMetrics,
    val header: CatalogHeaderMetrics,
) {
    val largeCanvas: Boolean get() = header.isLargeCanvas
    val isPhoneLandscape: Boolean get() = header.isPhoneLandscape
    val isTv42: Boolean get() = header.isTv42
    val isTv66: Boolean get() = header.isTv66
    val isLandscape: Boolean get() = profile.isLandscape
    val isPhone: Boolean
        get() = profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE ||
            profile.tier == DeviceProfileTier.COMPACT_PORTRAIT

    /** Título dinámico y “Clase terapéutica”. */
    val titleSp: Int get() = CatalogHeaderMetrics.catalogTitleSp(nav.titleFontSize.value)

    val horizontalPadding: Dp get() = grid.horizontalPadding
    val contentPadding: Dp get() = grid.contentPadding
    val gridMaxWidth: Dp get() = grid.maxContentWidth
    val topPadding: Dp get() = grid.topPadding
}

@Immutable
data class CatalogHeaderMetrics(
    val isPhoneLandscape: Boolean,
    val isTv42: Boolean,
    val isTv66: Boolean,
    val isLargeCanvas: Boolean,
    val badgeWidth: Dp,
    val titleStartGap: Dp,
    /** Volver y Home — un solo tamaño; cambiar aquí afecta ambos. */
    val navButtonSize: Dp,
    val searchBarWidth: Dp,
    val searchBarHeight: Dp,
    val filterIconSize: Dp,
    /** Empuje del filtro hacia la derecha (espacios teclado). */
    val filterOffsetX: Dp,
    val filterToSearchGap: Dp,
    val searchToNavGap: Dp,
    val sortTopGap: Dp,
    val searchIconSize: Dp,
    val searchFontSize: TextUnit,
) {
    fun centerOnSearchBar(controlSize: Dp): Dp =
        ((searchBarHeight - controlSize) / 2).coerceAtLeast(0.dp)

    companion object {
        private const val TITLE_SCALE = 0.95f
        /** Filtro: −5% −5% −2% −2% −10% (último pedido). */
        private const val FILTER_SCALE = 0.95f * 0.95f * 0.98f * 0.98f * 0.90f
        /** Nav catálogo (volver + home): unificado; −2% sobre el tamaño validado 36.dp. */
        private val CATALOG_NAV_BUTTON_SIZE = 36.dp * 0.98f
        private const val FILTER_SEARCH_EXTRA_SPACES = 3
        private const val FILTER_OFFSET_SPACES = 2
        private const val GRID_TOP_SPACES = 2

        fun catalogTitleSp(navTitleSp: Float): Int =
            ((navTitleSp + 2f) * TITLE_SCALE).roundToInt().coerceAtLeast(1)

        fun isLargeCatalogCanvas(profile: DeviceProfile, canvasHeight: Dp): Boolean =
            when (profile.tier) {
                DeviceProfileTier.TV_LARGE,
                DeviceProfileTier.TV_REGULAR,
                -> true
                else -> canvasHeight >= 700.dp
            }

        /** Padding top del grid de productos (bajar cards N espacios). */
        fun productsGridTopPadding(base: Dp = 16.dp): Dp =
            base + FireTv42Spacing.spaces(GRID_TOP_SPACES)

        /**
         * Padding end del scroll: borde derecho del rail = borde derecho del Home
         * (guía de la captura). CT + Productos.
         */
        fun scrollEndUnderHome(contentPadding: Dp): Dp = contentPadding

        fun resolve(
            profile: DeviceProfile,
            uiMetrics: TreatmentUiMetrics.ProfileMetrics,
            largeCanvas: Boolean,
            isLandscape: Boolean,
        ): CatalogHeaderMetrics {
            val isPhoneLandscape = profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE
            val isTv66 = profile.tier == DeviceProfileTier.TV_LARGE
            val isTv42 = profile.tier == DeviceProfileTier.TV_REGULAR
            val badgeWidth = uiMetrics.badgeHeight * TreatmentUiMetrics.BADGE_WIDTH_TO_HEIGHT

            val searchBarWidth = when {
                isTv66 -> 480.dp
                largeCanvas -> 280.dp
                isTv42 -> 240.dp
                isPhoneLandscape -> 196.dp
                isLandscape -> 256.dp
                else -> 175.dp
            }
            val searchBarHeight = when {
                largeCanvas -> 44.dp
                isTv42 -> 32.dp
                isPhoneLandscape -> 28.dp
                isLandscape -> 30.dp
                else -> 28.dp
            }
            val filterBase = when {
                largeCanvas -> 44.dp
                isTv42 -> 30.dp
                isPhoneLandscape -> 24.dp
                isLandscape -> 28.dp
                else -> 26.dp
            }
            val filterSearchExtra =
                if (isPhoneLandscape || isLandscape || largeCanvas || isTv42 || isTv66) {
                    FireTv42Spacing.spaces(FILTER_SEARCH_EXTRA_SPACES)
                } else {
                    0.dp
                }
            val filterToSearchGap = when {
                isPhoneLandscape -> 10.dp
                largeCanvas -> 16.dp
                isTv42 -> 14.dp
                else -> 12.dp
            } + filterSearchExtra
            val searchToNavGap = when {
                isPhoneLandscape -> 12.dp
                largeCanvas -> 16.dp
                isTv42 -> 12.dp
                else -> 14.dp
            }
            val sortTopGap = when {
                isPhoneLandscape -> 4.dp
                largeCanvas -> 12.dp
                else -> 5.dp
            }
            val searchIconSize = when {
                largeCanvas -> 22.dp
                isLandscape -> 16.dp
                else -> 14.dp
            }
            val searchFontSize = when {
                largeCanvas -> 15.sp
                isTv66 -> 13.sp
                isLandscape -> 12.sp
                else -> 11.sp
            }

            return CatalogHeaderMetrics(
                isPhoneLandscape = isPhoneLandscape,
                isTv42 = isTv42,
                isTv66 = isTv66,
                isLargeCanvas = largeCanvas,
                badgeWidth = badgeWidth,
                // Mismo origen X que el grid de cards (CT y Productos).
                titleStartGap = 0.dp,
                navButtonSize = CATALOG_NAV_BUTTON_SIZE,
                searchBarWidth = searchBarWidth,
                searchBarHeight = searchBarHeight,
                filterIconSize = filterBase * FILTER_SCALE,
                filterOffsetX = FireTv42Spacing.spaces(FILTER_OFFSET_SPACES),
                filterToSearchGap = filterToSearchGap,
                searchToNavGap = searchToNavGap,
                sortTopGap = sortTopGap,
                searchIconSize = searchIconSize,
                searchFontSize = searchFontSize,
            )
        }
    }
}

/** Resuelve perfil + grid + header de catálogo (CT y Productos). */
@Composable
fun rememberCatalogLayout(maxWidth: Dp, maxHeight: Dp): CatalogLayout {
    val density = LocalDensity.current
    val context = LocalContext.current
    val preferTv66 = TvProfileDetector.isTv66Candidate(
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        density = density,
        context = context,
    )
    return remember(maxWidth, maxHeight, preferTv66) {
        val screen = DeviceProfileResolver.screenMetrics(
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            preferTv66 = preferTv66,
        )
        val largeCanvas = CatalogHeaderMetrics.isLargeCatalogCanvas(screen.profile, maxHeight)
        val ui = TreatmentUiMetrics.forProfile(screen.profile, largeCanvas = largeCanvas)
        val header = CatalogHeaderMetrics.resolve(
            profile = screen.profile,
            uiMetrics = ui,
            largeCanvas = largeCanvas,
            isLandscape = screen.profile.isLandscape,
        )
        CatalogLayout(
            profile = screen.profile,
            grid = screen.grid,
            nav = screen.nav,
            ui = ui,
            header = header,
        )
    }
}

@Composable
fun CatalogScreenTitle(
    text: String,
    nav: SharedNavMetrics,
    titleStartGap: Dp,
    modifier: Modifier = Modifier,
) {
    LaSanteScreenTitle(
        text = text,
        fontSize = CatalogHeaderMetrics.catalogTitleSp(nav.titleFontSize.value),
        textColor = LaSanteText,
        underlineBrush = Brush.horizontalGradient(
            listOf(Color(0xFF8FA88A), Color(0xFFD5D8D2), Color.White),
        ),
        underlineWidth = nav.titleUnderlineWidth,
        underlineMatchTextWidth = true,
        textAlign = TextAlign.Start,
        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
        fontWeight = FontWeight.Light,
        allCaps = false,
        modifier = modifier.padding(start = titleStartGap),
    )
}

/** Log de header CT/Productos — tag [CatalogProfile]. */
@Composable
fun LogCatalogHeaderProfile(header: CatalogHeaderMetrics, screen: String) {
    androidx.compose.runtime.LaunchedEffect(
        screen,
        header.titleStartGap,
        header.navButtonSize,
        header.isLargeCanvas,
    ) {
        android.util.Log.i(
            "CatalogProfile",
            "$screen titleStartGap=${header.titleStartGap} " +
                "navBtn=${header.navButtonSize} " +
                "large=${header.isLargeCanvas} tv42=${header.isTv42} tv66=${header.isTv66}",
        )
    }
}
