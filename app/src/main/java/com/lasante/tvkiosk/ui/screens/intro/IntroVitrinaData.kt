package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.lasante.tvkiosk.data.VitrinaUnit

/**
 * Unidades en el mismo orden angular del GLB / [VitrinaGlbMapping].
 * El API puede traer Specialty antes que PHQ; aquí se reordena para que
 * `activeIndex` = cara del cilindro = ID de navegación.
 */
@Composable
fun rememberDisplayVitrinaUnits(
    vitrinaUnits: List<VitrinaUnit>,
): List<VitrinaUnit> {
    return remember(vitrinaUnits) {
        VitrinaGlbMapping.orderUnitsLikeGlb(vitrinaUnits)
    }
}
