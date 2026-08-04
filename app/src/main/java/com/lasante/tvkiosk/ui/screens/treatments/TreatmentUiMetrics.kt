package com.lasante.tvkiosk.ui.screens.treatments

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasante.tvkiosk.ui.layout.DeviceProfile
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier

/**
 * Métricas de badge (etiqueta) y cards de clases terapéuticas.
 * Icono protagonista (~88% del cuadrado) en todos los perfiles — mockup Clase terapéutica.
 */
object TreatmentUiMetrics {
    /** treatment_badge_shadow.png — 509×706 px recortado. */
    const val BADGE_WIDTH_TO_HEIGHT = 509f / 706f

    /** Fracción del área cuadrada que ocupa el icono (margen tipo mockup). */
    const val CARD_ICON_FILL = 0.94f

    @Immutable
    data class ProfileMetrics(
        val badgeHeight: Dp,
        val badgeIconSize: Dp,
        val badgeIconTop: Dp,
        /** Tamaño de decode Coil (calidad); el layout usa fill del cuadrado. */
        val cardIconSize: Dp,
        val cardMaxWidth: Dp,
        val navButtonSize: Dp,
        /** Espacio a la derecha de Home/Back para no quedar bajo la etiqueta (badge). */
        val navEndInset: Dp = 0.dp,
        val cardLabelFontSize: TextUnit = 12.sp,
        val cardLabelLineHeight: TextUnit = 14.sp,
    )

    /** Infinix / phone landscape. */
    val phoneLandscape = ProfileMetrics(
        badgeHeight = 46.dp,
        badgeIconSize = 37.dp,
        badgeIconTop = 0.dp,
        cardIconSize = 200.dp,
        cardMaxWidth = 200.dp,
        navButtonSize = 32.dp,
        navEndInset = 0.dp,
        cardLabelFontSize = 12.sp,
        cardLabelLineHeight = 14.sp,
    )

    /** Phone portrait (~360×800 dp). */
    val phonePortrait = ProfileMetrics(
        badgeHeight = 62.dp,
        badgeIconSize = 44.dp,
        badgeIconTop = 0.dp,
        cardIconSize = 200.dp,
        cardMaxWidth = 200.dp,
        navButtonSize = 35.dp,
        cardLabelFontSize = 13.sp,
        cardLabelLineHeight = 15.sp,
    )

    /** TV 42" / Fire / Damasco (tv_42). */
    val tv42 = ProfileMetrics(
        badgeHeight = 64.dp,
        badgeIconSize = 51.dp,
        badgeIconTop = 0.dp,
        cardIconSize = 260.dp,
        cardMaxWidth = 260.dp,
        navButtonSize = 38.dp,
        navEndInset = 0.dp,
        cardLabelFontSize = 13.sp,
        cardLabelLineHeight = 15.sp,
    )

    /** @deprecated Alias de [tv42]. */
    val tv: ProfileMetrics
        get() = tv42

    /** TV 66". */
    val tv66 = ProfileMetrics(
        badgeHeight = 91.dp,
        badgeIconSize = 56.dp,
        badgeIconTop = 0.dp,
        cardIconSize = 300.dp,
        cardMaxWidth = 300.dp,
        navButtonSize = 53.dp,
        navEndInset = 90.dp,
        cardLabelFontSize = 20.sp,
        cardLabelLineHeight = 24.sp,
    )

    fun forProfile(profile: DeviceProfile): ProfileMetrics = when (profile.tier) {
        DeviceProfileTier.COMPACT_LANDSCAPE -> phoneLandscape
        DeviceProfileTier.COMPACT_PORTRAIT -> phonePortrait
        DeviceProfileTier.TV_LARGE -> tv66
        DeviceProfileTier.TV_REGULAR -> tv42
        DeviceProfileTier.TABLET_LANDSCAPE, DeviceProfileTier.DEFAULT -> if (profile.isWide) tv42 else phonePortrait
    }

    /** @deprecated Usar [forProfile]. */
    fun profile(
        isPhoneLandscape: Boolean = false,
        isTv66: Boolean = false,
        isTv42: Boolean = false,
        isTv: Boolean = false,
        isWide: Boolean = false,
        isLandscape: Boolean = false,
    ): ProfileMetrics = when {
        isPhoneLandscape -> phoneLandscape
        isTv66 -> tv66
        isTv42 || (isTv && !isTv66) -> tv42
        isWide -> tv42
        isLandscape -> phoneLandscape
        else -> phonePortrait
    }

    /** Iconos Hospital Care para preview / calibración (distintos aspect ratios). */
    val hospitalCareIconSlugs = listOf(
        "antiinfeccioso",
        "cardiovascular",
        "digestivo-metabolico",
        "sistema-nervioso-central",
    )
}
