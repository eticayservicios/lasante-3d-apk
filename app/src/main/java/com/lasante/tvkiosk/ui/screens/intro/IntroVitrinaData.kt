package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.lasante.tvkiosk.data.VitrinaUnit

@Composable
fun rememberDisplayVitrinaUnits(
    vitrinaUnits: List<VitrinaUnit>,
): List<VitrinaUnit> {
    return remember(vitrinaUnits) { vitrinaUnits }
}
