package com.lasante.tvkiosk.ui.screens.treatments

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lasante.tvkiosk.ui.layout.DeviceProfile
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier

/**
 * Métricas de badge (etiqueta) y cards de clases terapéuticas.
 * Derivadas del perfil unificado [DeviceProfile].
 */
object TreatmentUiMetrics {
    /** treatment_badge_shadow.png — 509×706 px recortado. */
    const val BADGE_WIDTH_TO_HEIGHT = 509f / 706f

    @Immutable
    data class ProfileMetrics(
        val badgeHeight: Dp,
        val badgeIconSize: Dp,
        val badgeIconTop: Dp,
        val cardIconSize: Dp,
        val cardMaxWidth: Dp,
        val navButtonSize: Dp,
        /** Espacio a la derecha de Home/Back para no quedar bajo la etiqueta (badge). */
        val navEndInset: Dp = 0.dp,
    )

    /** Infinix: badge −20% vs base; cards −10% (diseño). */
    val phoneLandscape = ProfileMetrics(
        badgeHeight = 46.dp,
        badgeIconSize = 37.dp,
        badgeIconTop = 0.dp,
        cardIconSize = 116.dp,
        cardMaxWidth = 133.dp,
        navButtonSize = 32.dp,
        navEndInset = 0.dp,
    )

    /** Phone portrait (~360×800 dp). */
    val phonePortrait = ProfileMetrics(
        badgeHeight = 62.dp,
        badgeIconSize = 44.dp,
        badgeIconTop = 0.dp,
        cardIconSize = 139.dp,
        cardMaxWidth = 146.dp,
        navButtonSize = 35.dp,
    )

    /** TV 42" (~960×540 dp). Cards −10% vs base. */
    val tv42 = ProfileMetrics(
        badgeHeight = 64.dp,
        badgeIconSize = 51.dp,
        badgeIconTop = 0.dp,
        cardIconSize = 127.dp,
        cardMaxWidth = 144.dp,
        navButtonSize = 38.dp,
        navEndInset = 0.dp,
    )

    /** @deprecated Alias de [tv42]. */
    val tv: ProfileMetrics
        get() = tv42

    /** TV 66" (~1920×1080 dp). */
    val tv66 = ProfileMetrics(
        badgeHeight = 91.dp,
        badgeIconSize = 56.dp,
        badgeIconTop = 0.dp,
        cardIconSize = 156.dp,
        cardMaxWidth = 212.dp,
        navButtonSize = 53.dp,
        navEndInset = 90.dp,
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
