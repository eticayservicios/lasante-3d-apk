package com.lasante.tvkiosk.ui.screens.treatments

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasante.tvkiosk.ui.layout.DeviceProfile
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier
import com.lasante.tvkiosk.ui.layout.Tv42Spacing

/**
 * Badge + cards de clases terapéuticas.
 * [tv42Large] = catálogo LARGE (TV_REGULAR / tablet large / TV1080).
 */
object TreatmentUiMetrics {
    const val BADGE_WIDTH_TO_HEIGHT = 509f / 706f
    const val CARD_ICON_FILL = 0.70f
    const val CARD_ICON_FILL_LARGE = 0.86f

    /** −10% × −5% en rótulos de clase (todos los perfiles). */
    private const val LABEL_SCALE = 0.9f * 0.95f

    private fun scaledSp(base: TextUnit): TextUnit = base * LABEL_SCALE

    @Immutable
    data class ProfileMetrics(
        val badgeHeight: Dp,
        val badgeIconSize: Dp,
        val cardIconSize: Dp,
        val navButtonSize: Dp,
        val cardLabelFontSize: TextUnit = 12.sp,
        val cardLabelLineHeight: TextUnit = 14.sp,
        val cardIconFill: Float = CARD_ICON_FILL,
        val cardAspectRatio: Float = 1f,
    )

    val tv42 = ProfileMetrics(
        // Fondo +2 esp; icono −1 esp (patrón Hikvision escalado).
        badgeHeight = 64.dp + Tv42Spacing.spaces(2),
        badgeIconSize = 28.dp - Tv42Spacing.spaces(1),
        cardIconSize = 320.dp,
        navButtonSize = 38.dp,
        cardLabelFontSize = scaledSp(12.sp),
        cardLabelLineHeight = scaledSp(14.sp),
        // TV42: iconos −30% vs fill previo 0.62.
        cardIconFill = 0.62f * 0.70f,
        cardAspectRatio = 1.07f,
    )

    val phoneLandscape = ProfileMetrics(
        badgeHeight = 46.dp + Tv42Spacing.spaces(1),
        badgeIconSize = (22.dp - Tv42Spacing.spaces(1)).coerceAtLeast(16.dp),
        cardIconSize = 240.dp,
        navButtonSize = 32.dp,
        cardLabelFontSize = scaledSp(12.sp),
        cardLabelLineHeight = scaledSp(14.sp),
    )

    val phonePortrait = ProfileMetrics(
        badgeHeight = 62.dp + Tv42Spacing.spaces(1),
        badgeIconSize = (26.dp - Tv42Spacing.spaces(1)).coerceAtLeast(18.dp),
        cardIconSize = 240.dp,
        navButtonSize = 35.dp,
        cardLabelFontSize = scaledSp(13.sp),
        cardLabelLineHeight = scaledSp(15.sp),
    )

    val tv42Large = ProfileMetrics(
        // TV1080/large: fondo +3 esp; icono −2 esp.
        badgeHeight = 78.dp + Tv42Spacing.spaces(3),
        badgeIconSize = 38.dp - Tv42Spacing.spaces(2),
        cardIconSize = 560.dp,
        navButtonSize = 52.dp,
        cardLabelFontSize = scaledSp(20.sp),
        cardLabelLineHeight = scaledSp(23.sp),
        // Television_1080 / TV1080: iconos −30% (fill 0.86 → 0.60).
        cardIconFill = CARD_ICON_FILL_LARGE * 0.70f,
        cardAspectRatio = 1.12f,
    )

    val tv66 = ProfileMetrics(
        // Fondo +4 espacios; icono 38 − 2 espacios.
        badgeHeight = 104.dp + Tv42Spacing.spaces(4),
        badgeIconSize = 38.dp - Tv42Spacing.spaces(2),
        cardIconSize = 420.dp,
        navButtonSize = 52.dp,
        cardLabelFontSize = scaledSp(18.sp),
        cardLabelLineHeight = scaledSp(21.sp),
        // Hikvision físico: más reducido para que el icono no se corte en el card ni al borde.
        cardIconFill = 0.70f * 0.55f,
        cardAspectRatio = 1.14f,
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
}
