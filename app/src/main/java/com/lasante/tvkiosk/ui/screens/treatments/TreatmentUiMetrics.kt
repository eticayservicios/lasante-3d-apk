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
 * Icono protagonista en todos los perfiles — mockup Clase terapéutica.
 *
 * [tv42] = Fire / TV 42 baseline (~961×529).
 * [tv42Large] = Damasco / canvas alto — iconos y rótulos más grandes.
 */
object TreatmentUiMetrics {
    /** treatment_badge_shadow.png — 509×706 px recortado. */
    const val BADGE_WIDTH_TO_HEIGHT = 509f / 706f

    /** Fracción del área de icono (Fire / Infinix). */
    const val CARD_ICON_FILL = 0.70f

    /** Damasco / TV grande — iconos protagonistas (mockup + feedback jefe). */
    const val CARD_ICON_FILL_LARGE = 0.86f

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
        val cardIconFill: Float = CARD_ICON_FILL,
        /** width/height del card. 1f = cuadrado (mockup). */
        val cardAspectRatio: Float = 1f,
    )

    /** TV 42" / Fire baseline. */
    val tv42 = ProfileMetrics(
        badgeHeight = 64.dp,
        badgeIconSize = 28.dp,
        badgeIconTop = 6.dp,
        cardIconSize = 320.dp,
        cardMaxWidth = 260.dp,
        navButtonSize = 38.dp,
        navEndInset = 0.dp,
        cardLabelFontSize = 13.sp,
        cardLabelLineHeight = 15.sp,
        cardIconFill = CARD_ICON_FILL,
    )

    /** Infinix / phone landscape. */
    val phoneLandscape = ProfileMetrics(
        badgeHeight = 46.dp,
        badgeIconSize = 22.dp,
        badgeIconTop = 5.dp,
        cardIconSize = 240.dp,
        cardMaxWidth = 200.dp,
        navButtonSize = 32.dp,
        navEndInset = 0.dp,
        cardLabelFontSize = 12.sp,
        cardLabelLineHeight = 14.sp,
        cardIconFill = CARD_ICON_FILL,
    )

    /** Phone portrait (~360×800 dp). */
    val phonePortrait = ProfileMetrics(
        badgeHeight = 62.dp,
        badgeIconSize = 26.dp,
        badgeIconTop = 6.dp,
        cardIconSize = 240.dp,
        cardMaxWidth = 200.dp,
        navButtonSize = 35.dp,
        cardLabelFontSize = 13.sp,
        cardLabelLineHeight = 15.sp,
        cardIconFill = CARD_ICON_FILL,
    )

    /** Damasco / canvas alto — iconos y textos más grandes (mockup). */
    val tv42Large = ProfileMetrics(
        badgeHeight = 78.dp,
        badgeIconSize = 38.dp,
        badgeIconTop = 7.dp,
        cardIconSize = 560.dp,
        cardMaxWidth = 360.dp,
        navButtonSize = 52.dp,
        navEndInset = 0.dp,
        cardLabelFontSize = 20.sp,
        cardLabelLineHeight = 23.sp,
        cardIconFill = CARD_ICON_FILL_LARGE,
        // Más ancho que alto → menos vacío vertical, icono ocupa casi todo.
        cardAspectRatio = 1.12f,
    )

    /** @deprecated Alias de [tv42]. */
    val tv: ProfileMetrics
        get() = tv42

    /** TV 66". */
    val tv66 = ProfileMetrics(
        badgeHeight = 100.dp,
        badgeIconSize = 44.dp,
        badgeIconTop = 9.dp,
        cardIconSize = 640.dp,
        cardMaxWidth = 400.dp,
        navButtonSize = 60.dp,
        navEndInset = 90.dp,
        cardLabelFontSize = 24.sp,
        cardLabelLineHeight = 28.sp,
        cardIconFill = CARD_ICON_FILL_LARGE,
        cardAspectRatio = 1.12f,
    )

    fun forProfile(
        profile: DeviceProfile,
        largeCanvas: Boolean = false,
    ): ProfileMetrics = when (profile.tier) {
        DeviceProfileTier.COMPACT_LANDSCAPE -> phoneLandscape
        DeviceProfileTier.COMPACT_PORTRAIT -> phonePortrait
        DeviceProfileTier.TV_LARGE -> tv66
        DeviceProfileTier.TV_REGULAR -> if (largeCanvas) tv42Large else tv42
        DeviceProfileTier.TABLET_LANDSCAPE, DeviceProfileTier.DEFAULT ->
            if (largeCanvas) tv42Large else if (profile.isWide) tv42 else phonePortrait
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
