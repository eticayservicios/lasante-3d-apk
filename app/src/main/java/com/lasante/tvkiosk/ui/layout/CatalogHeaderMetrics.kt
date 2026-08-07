package com.lasante.tvkiosk.ui.layout

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasante.tvkiosk.ui.components.LaSanteScreenTitle
import com.lasante.tvkiosk.ui.screens.treatments.TreatmentUiMetrics
import com.lasante.tvkiosk.ui.theme.LaSanteText

/**
 * Geometría compartida del header CT ↔ Productos.
 *
 * Catálogo LARGE (btn 52, search 280, title gap large): TV_REGULAR completo
 * (TV1080 / Fire / Ariana 1137×711 / Damasco) y TV_LARGE.
 * 1 “espacio” = [FireTv42Spacing.KeyboardSpace] (Poppins U+0020 @ 24.sp ≈ 6.408.dp).
 *
 * [isFireTv42] queda false en catálogo TV_REGULAR (misma geometría que Ariana LARGE).
 */
@Immutable
data class CatalogHeaderMetrics(
    val isPhoneLandscape: Boolean,
    val isTv42: Boolean,
    val isTv66: Boolean,
    val isLargeCanvas: Boolean,
    val isFireTv42: Boolean,
    val badgeWidth: Dp,
    val titleStartGap: Dp,
    val navButtonSize: Dp,
    val searchBarWidth: Dp,
    val searchBarHeight: Dp,
    val filterIconSize: Dp,
    val filterToSearchGap: Dp,
    val searchToNavGap: Dp,
    val sortTopGap: Dp,
    val searchIconSize: Dp,
    val searchFontSize: TextUnit,
) {
    /** Padding para centrar un control de [controlSize] en la altura del buscador. */
    fun centerOnSearchBar(controlSize: Dp): Dp =
        ((searchBarHeight - controlSize) / 2).coerceAtLeast(0.dp)

    companion object {
        /**
         * Catálogo: TV1080/Fire alineado a Ariana (1137×711, LARGE, btn 52).
         * Antes Fire quedaba fuera por altura &lt; 700.dp.
         */
        fun isLargeCatalogCanvas(profile: DeviceProfile, canvasHeight: Dp): Boolean =
            when (profile.tier) {
                DeviceProfileTier.TV_LARGE,
                DeviceProfileTier.TV_REGULAR,
                -> true
                else -> canvasHeight >= 700.dp
            }

        fun resolve(
            profile: DeviceProfile,
            uiMetrics: TreatmentUiMetrics.ProfileMetrics,
            largeCanvas: Boolean,
            isLandscape: Boolean,
        ): CatalogHeaderMetrics {
            val isPhoneLandscape = profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE
            val isTv66 = profile.tier == DeviceProfileTier.TV_LARGE
            val isTv42 = profile.tier == DeviceProfileTier.TV_REGULAR
            // Catálogo TV_REGULAR siempre large → offsets Fire de header quedan inactivos.
            val isFireTv42 = isTv42 && !largeCanvas
            val badgeWidth = uiMetrics.badgeHeight * TreatmentUiMetrics.BADGE_WIDTH_TO_HEIGHT

            // phone 48 · large (TV1080/Ariana/Damasco) 44 · fire residual 34−2esp · tv42 34 · else 32
            val titleExtra = when {
                isPhoneLandscape -> 48.dp
                largeCanvas -> 44.dp
                isFireTv42 -> 34.dp - FireTv42Spacing.spaces(2)
                isTv42 -> 34.dp
                else -> 32.dp
            }

            val navButtonSize = if (isFireTv42) {
                uiMetrics.navButtonSize * 0.95f
            } else {
                uiMetrics.navButtonSize
            }

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
            val filterIconSize = when {
                // Ariana/LARGE: 44 −5% −5% (segundo ajuste).
                largeCanvas -> 44.dp * 0.95f * 0.95f
                isFireTv42 -> 30.dp * 0.95f
                isTv42 -> 30.dp
                isPhoneLandscape -> 24.dp
                isLandscape -> 28.dp
                else -> 26.dp
            }
            val filterToSearchGap = when {
                isPhoneLandscape -> 10.dp
                largeCanvas -> 16.dp
                isFireTv42 -> 14.dp + FireTv42Spacing.spaces(3)
                isTv42 -> 14.dp
                else -> 12.dp
            }
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
                isFireTv42 = isFireTv42,
                badgeWidth = badgeWidth,
                titleStartGap = badgeWidth + titleExtra,
                navButtonSize = navButtonSize,
                searchBarWidth = searchBarWidth,
                searchBarHeight = searchBarHeight,
                filterIconSize = filterIconSize,
                filterToSearchGap = filterToSearchGap,
                searchToNavGap = searchToNavGap,
                sortTopGap = sortTopGap,
                searchIconSize = searchIconSize,
                searchFontSize = searchFontSize,
            )
        }
    }
}

/** Título principal compartido CT ↔ Productos (misma X/Y). */
@Composable
fun CatalogScreenTitle(
    text: String,
    nav: SharedNavMetrics,
    titleStartGap: Dp,
    modifier: Modifier = Modifier,
) {
    val titleSp = ((nav.titleFontSize.value + 2f) * 0.95f).toInt().coerceAtLeast(1)
    LaSanteScreenTitle(
        text = text,
        fontSize = titleSp,
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
        // Solo start gap — sin weight ni top pad (misma X/Y CT ↔ Productos).
        modifier = modifier.padding(start = titleStartGap),
    )
}
