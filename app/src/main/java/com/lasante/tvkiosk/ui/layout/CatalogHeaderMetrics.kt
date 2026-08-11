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
    /**
     * Baja el título junto con filtro/búsqueda/nav ([controlsTopGap]).
     * Ordenar usa [sortTopGap] aparte.
     */
    val titleTopGap: Dp,
    /**
     * Empuje vertical compartido de filtro + barra de búsqueda + botones nav
     * (misma magnitud que el título en la familia Fire/Ariana/TV66).
     */
    val controlsTopGap: Dp,
    /** Volver y Home — un solo tamaño; cambiar aquí afecta ambos. */
    val navButtonSize: Dp,
    val searchBarWidth: Dp,
    val searchBarHeight: Dp,
    val filterIconSize: Dp,
    /** Empuje del filtro hacia la derecha (espacios teclado). */
    val filterOffsetX: Dp,
    val filterToSearchGap: Dp,
    val searchToNavGap: Dp,
    /** Espacio entre Volver y Home (TV66: 3 espacios). */
    val navPairSpacing: Dp,
    val sortTopGap: Dp,
    /** Escala del botón Ordenar (TV66: 1.10). */
    val sortScale: Float,
    val searchIconSize: Dp,
    val searchFontSize: TextUnit,
    /**
     * Fire / Tablet Ariana / TV66: misma familia de nudges verticales de header.
     */
    val usesSharedTvCatalogLayout: Boolean,
    /** Ancho del bloque completo de cada producto (cuadro + textos). */
    val productBlockWidthFraction: Float,
) {
    fun centerOnSearchBar(controlSize: Dp): Dp =
        ((searchBarHeight - controlSize) / 2).coerceAtLeast(0.dp)

    /**
     * Padding top unificado de Volver/Home — misma Y en CT y Productos.
     * Familia shared TV (Fire/Ariana/TV66): 0 — el Row usa [controlsTopGap] + CenterVertically.
     */
    fun navButtonsTopGap(buttonSize: Dp = navButtonSize): Dp =
        if (usesSharedTvCatalogLayout) 0.dp else centerOnSearchBar(buttonSize) + controlsTopGap

    companion object {
        private const val TITLE_SCALE = 0.95f
        /** Nav catálogo (volver + home): unificado; −2% sobre el tamaño validado 36.dp. */
        private val CATALOG_NAV_BUTTON_SIZE = 36.dp * 0.98f
        private const val FILTER_OFFSET_SPACES = 2
        private const val GRID_TOP_SPACES = 2
        /** Productos: bajar cards (+2 espacios vs base). */
        private const val PRODUCTS_GRID_TOP_SPACES = 4
        /** Baja el título principal 2 espacios (base global). Shared TV: se anula. */
        private const val TITLE_TOP_SPACES = 2
        /** Fire/Ariana/TV66: título + filtro + búsqueda + nav, juntos. */
        private const val CATALOG_HEADER_TOP_SPACES = 2
        /** Fire/Ariana/TV66: Ordenar A-Z, solo ese botón. */
        private const val CATALOG_SORT_TOP_SPACES = 3
        /** TV66: bloques de controles. Fire/Ariana: −1 espacio (escala). */
        private const val TV66_CONTROL_BLOCK_SPACES = 3
        private const val SHARED_TV_CONTROL_BLOCK_SPACES = 2
        /** Filtro ↔ buscador: TV66 = 3 esp; Fire/Ariana/phone = 2 esp (escala). */
        private const val TV66_FILTER_TO_SEARCH_SPACES = 3
        private const val SHARED_FILTER_TO_SEARCH_SPACES = 2
        /** Ancho bloque producto: TV66 0.90 → Fire/Ariana 0.92 → resto 1. */
        private const val PRODUCT_BLOCK_WIDTH_TV42 = 0.92f
        private const val PRODUCT_BLOCK_WIDTH_TV66 = 0.90f
        private const val CT_CARD_WIDTH_TV66 = 0.90f
        /** Nav: TV66 1.15×1.05; Fire/Ariana ×1.08 (escala). */
        private const val TV66_NAV_SCALE = 1.15f * 1.05f
        private const val SHARED_TV_NAV_SCALE = 1.08f
        private const val TV66_SORT_SCALE = 1.10f
        private const val SHARED_TV_SORT_SCALE = 1.05f

        fun catalogTitleSp(navTitleSp: Float): Int =
            ((navTitleSp + 2f) * TITLE_SCALE).roundToInt().coerceAtLeast(1)

        /** Fire, Ariana (largeCanvas) y TV66: misma familia de header. */
        fun usesSharedTvCatalogLayout(
            isTv42: Boolean,
            isTv66: Boolean,
            largeCanvas: Boolean = false,
        ): Boolean = isTv42 || isTv66 || largeCanvas

        fun productBlockWidthFraction(isTv42: Boolean, isTv66: Boolean): Float = when {
            isTv66 -> PRODUCT_BLOCK_WIDTH_TV66
            isTv42 -> PRODUCT_BLOCK_WIDTH_TV42
            else -> 1f
        }

        /** Ancho del card de clase terapéutica (CT). */
        fun ctCardWidthFraction(
            isTv66: Boolean,
            isTv42: Boolean,
            largeCanvas: Boolean = false,
        ): Float = when {
            isTv66 -> CT_CARD_WIDTH_TV66
            isTv42 || largeCanvas -> PRODUCT_BLOCK_WIDTH_TV42
            else -> 1f
        }

        fun isLargeCatalogCanvas(profile: DeviceProfile, canvasHeight: Dp): Boolean =
            when (profile.tier) {
                DeviceProfileTier.TV_LARGE,
                DeviceProfileTier.TV_REGULAR,
                -> true
                else -> canvasHeight >= 700.dp
            }

        /** Padding top del grid de productos (bajar cards N espacios). */
        fun productsGridTopPadding(base: Dp = 16.dp): Dp =
            base + FireTv42Spacing.spaces(PRODUCTS_GRID_TOP_SPACES)

        /** Padding top del grid de clases terapéuticas.
         * Shared TV: 0 (subtítulo+cards ya suben). Otros: 2 espacios.
         */
        fun treatmentsGridTopPadding(sharedTv: Boolean = false): Dp =
            if (sharedTv) 0.dp else FireTv42Spacing.spaces(GRID_TOP_SPACES)

        /**
         * Padding end del scroll: borde derecho del rail = borde derecho del Home
         * (guía de la captura). CT + Productos.
         */
        fun scrollEndUnderHome(contentPadding: Dp): Dp = contentPadding

        /**
         * Inset izquierdo del card dentro de la celda (block centrado con fraction < 1).
         * Así el título queda **al ras** del borde izquierdo de los cards CT/Productos.
         */
        fun titleAlignWithCardInset(
            profile: DeviceProfile,
            grid: SharedGridMetrics,
            blockWidthFraction: Float,
            columns: Int = grid.columns,
        ): Dp {
            if (blockWidthFraction >= 0.999f) return 0.dp
            val columnWidth = minOf(profile.maxWidth, grid.maxContentWidth)
            val inner = (columnWidth - grid.horizontalPadding * 2f).coerceAtLeast(0.dp)
            val gridInner = (inner - grid.contentPadding * 2f).coerceAtLeast(0.dp)
            val gaps = grid.cardSpacing * (columns - 1).coerceAtLeast(0)
            val cell = ((gridInner - gaps) / columns).coerceAtLeast(0.dp)
            return cell * ((1f - blockWidthFraction) / 2f)
        }

        fun resolve(
            profile: DeviceProfile,
            uiMetrics: TreatmentUiMetrics.ProfileMetrics,
            largeCanvas: Boolean,
            isLandscape: Boolean,
        ): CatalogHeaderMetrics {
            val isPhoneLandscape = profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE
            // Cinturón + tirantes: canvas Hikvision fuerza métricas TV66 aunque el tier viniera mal.
            val isTv66 = profile.tier == DeviceProfileTier.TV_LARGE ||
                HikvisionLayoutDebug.isForced() ||
                Tv66Reference.matchesReferenceCanvas(profile.maxWidth, profile.maxHeight)
            val isTv42 = profile.tier == DeviceProfileTier.TV_REGULAR && !isTv66
            val badgeWidth = uiMetrics.badgeHeight * TreatmentUiMetrics.BADGE_WIDTH_TO_HEIGHT
            val sharedTv = usesSharedTvCatalogLayout(isTv42, isTv66, largeCanvas)

            val searchBarWidth = when {
                isTv66 -> 400.dp
                // Fire/Ariana: entre 280 y 400 (escala hacia Hikvision).
                largeCanvas || isTv42 -> 320.dp
                isPhoneLandscape -> 196.dp
                isLandscape -> 256.dp
                else -> 175.dp
            }
            val searchBarHeight = when {
                largeCanvas || isTv66 -> 44.dp
                isTv42 -> 32.dp
                isPhoneLandscape -> 28.dp
                isLandscape -> 30.dp
                else -> 28.dp
            }
            val controlBlockSpaces =
                if (isTv66) TV66_CONTROL_BLOCK_SPACES else SHARED_TV_CONTROL_BLOCK_SPACES
            val controlBlockGap = FireTv42Spacing.spaces(controlBlockSpaces)
            val filterToSearchGap = when {
                isTv66 -> FireTv42Spacing.spaces(TV66_FILTER_TO_SEARCH_SPACES)
                sharedTv || isPhoneLandscape ->
                    FireTv42Spacing.spaces(SHARED_FILTER_TO_SEARCH_SPACES)
                else -> 8.dp
            }
            val searchToNavGap = when {
                sharedTv -> controlBlockGap
                isPhoneLandscape -> 12.dp
                else -> 14.dp
            }
            val sortTopGapBase = when {
                isPhoneLandscape -> 4.dp
                largeCanvas || isTv66 -> 12.dp
                else -> 5.dp
            }
            // TV66 +4 esp; Fire/Ariana +2 esp extra (escala).
            val catalogHeaderTop = when {
                !sharedTv -> 0.dp
                isTv66 -> FireTv42Spacing.spaces(CATALOG_HEADER_TOP_SPACES + 4)
                else -> FireTv42Spacing.spaces(CATALOG_HEADER_TOP_SPACES + 2)
            }
            val catalogSortExtra =
                if (sharedTv) FireTv42Spacing.spaces(CATALOG_SORT_TOP_SPACES) else 0.dp
            val searchIconSize = when {
                largeCanvas || isTv66 -> 22.dp
                isLandscape -> 16.dp
                else -> 14.dp
            }
            val searchFontSize = when {
                largeCanvas -> 15.sp
                isTv66 -> 13.sp
                isLandscape -> 12.sp
                else -> 11.sp
            }
            val navButtonSize = when {
                isTv66 -> CATALOG_NAV_BUTTON_SIZE * TV66_NAV_SCALE
                sharedTv -> CATALOG_NAV_BUTTON_SIZE * SHARED_TV_NAV_SCALE
                else -> CATALOG_NAV_BUTTON_SIZE
            }
            val navPairSpacing = if (sharedTv) controlBlockGap else 0.dp
            val sortScale = when {
                isTv66 -> TV66_SORT_SCALE
                sharedTv -> SHARED_TV_SORT_SCALE
                else -> 1f
            }
            // Filtro = máximo(nav+2, alto buscador): llena el círculo sin quedar menor que Home/Back.
            val filterIconSize = maxOf(navButtonSize + 2.dp, searchBarHeight)

            return CatalogHeaderMetrics(
                isPhoneLandscape = isPhoneLandscape,
                isTv42 = isTv42,
                isTv66 = isTv66,
                isLargeCanvas = largeCanvas,
                badgeWidth = badgeWidth,
                // Shared TV: al ras (patrón Hikvision). Phone: +7%W.
                titleStartGap = if (sharedTv) 0.dp else profile.maxWidth * 0.07f,
                titleTopGap = if (sharedTv) {
                    0.dp
                } else {
                    FireTv42Spacing.spaces(TITLE_TOP_SPACES) + catalogHeaderTop
                },
                controlsTopGap = catalogHeaderTop,
                navButtonSize = navButtonSize,
                searchBarWidth = searchBarWidth,
                searchBarHeight = searchBarHeight,
                filterIconSize = filterIconSize,
                // Gap lo da filterToSearchGap (sin empujar extra).
                filterOffsetX = if (sharedTv || isPhoneLandscape) {
                    0.dp
                } else {
                    FireTv42Spacing.spaces(FILTER_OFFSET_SPACES)
                },
                filterToSearchGap = filterToSearchGap,
                searchToNavGap = searchToNavGap,
                navPairSpacing = navPairSpacing,
                sortTopGap = sortTopGapBase + catalogSortExtra,
                sortScale = sortScale,
                searchIconSize = searchIconSize,
                searchFontSize = searchFontSize,
                usesSharedTvCatalogLayout = sharedTv,
                productBlockWidthFraction = when {
                    isTv66 -> PRODUCT_BLOCK_WIDTH_TV66
                    isTv42 || largeCanvas -> PRODUCT_BLOCK_WIDTH_TV42
                    else -> 1f
                },
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
        val headerBase = CatalogHeaderMetrics.resolve(
            profile = screen.profile,
            uiMetrics = ui,
            largeCanvas = largeCanvas,
            isLandscape = screen.profile.isLandscape,
        )
        val header = if (headerBase.usesSharedTvCatalogLayout) {
            headerBase.copy(
                titleStartGap = CatalogHeaderMetrics.titleAlignWithCardInset(
                    profile = screen.profile,
                    grid = screen.grid,
                    blockWidthFraction = headerBase.productBlockWidthFraction,
                ),
            )
        } else {
            headerBase
        }
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
    titleTopGap: Dp = 0.dp,
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
        modifier = modifier.padding(start = titleStartGap, top = titleTopGap),
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
                "large=${header.isLargeCanvas} tv42=${header.isTv42} tv66=${header.isTv66} " +
                HikvisionLayoutDebug.overlayLabel(),
        )
    }
}
