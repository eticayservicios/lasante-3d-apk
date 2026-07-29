package com.lasante.tvkiosk.ui.screens.intro

import com.lasante.tvkiosk.BuildConfig

/**
 * Logs de proyección del cintillo — filtrar logcat: `VitrinaBandDiag`.
 */
object VitrinaUnitBandDiagnostics {
    private const val TAG = "VitrinaBandDiag"

    fun logScreenProjections(
        activeGlbIndex: Int,
        projectedUnits: Map<Int, FeaturedSlotScreenPoint>,
        rotationDegrees: Float,
    ) {
        if (!BuildConfig.DEBUG) return
        if (projectedUnits.isEmpty()) {
            VitrinaDebugLog.w(TAG, "screen activeGlbIndex=$activeGlbIndex rotation=${fmt(rotationDegrees)} projections=empty")
            return
        }
        projectedUnits.toSortedMap().forEach { (index, point) ->
            VitrinaDebugLog.d(
                TAG,
                "screen unit[$index] ${VitrinaGlbMapping.orderedGlbNodeNames.getOrNull(index)} " +
                    "active=${index == activeGlbIndex} ndc=(${fmt(point.xFraction)},${fmt(point.yFraction)})",
            )
        }
        val active = projectedUnits[activeGlbIndex]
        VitrinaDebugLog.d(
            TAG,
            "screen activeGlbIndex=$activeGlbIndex rotation=${fmt(rotationDegrees)} " +
                "activeNdc=(${fmt(active?.xFraction ?: -1f)},${fmt(active?.yFraction ?: -1f)})",
        )
    }

    private fun fmt(value: Float): String = "%.3f".format(value)
}
