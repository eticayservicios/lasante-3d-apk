package com.lasante.tvkiosk.ui.screens.intro

/**
 * Única fuente de verdad del cilindro.
 *
 * Orden angular = API = admin:
 * Genéricos → Primary → Specialty → PHQ → Hospital.
 *
 * En el GLB: nombre de nodo = texto del cintillo = [orderedNavigationUnitIds].
 * No invertir specialty/phq ni re-encodear Draco de esos meshes.
 */
object VitrinaGlbMapping {
    data class UnitFace(
        val index: Int,
        val nodeName: String,
        val unitId: String,
        val bearingDegrees: Float,
    )

    val faces: List<UnitFace> = listOf(
        UnitFace(0, "genericos_lasante", "genericos-la-sante", 90.417f),
        UnitFace(1, "primary_care", "primary-care", 162.405f),
        UnitFace(2, "specialty_care", "specialty-care", -125.567f),
        UnitFace(3, "phq_consumo", "phq-consumo", -53.593f),
        UnitFace(4, "hospital_care", "hospital-care", 18.440f),
    )

    val orderedGlbNodeNames: List<String> = faces.map { it.nodeName }

    val orderedNavigationUnitIds: List<String> = faces.map { it.unitId }

    private val apiUnitIdToGlbIndex = orderedNavigationUnitIds
        .mapIndexed { index, unitId -> unitId to index }
        .toMap() + mapOf("medicina-general" to 0)

    fun glbIndexFor(apiIndex: Int): Int =
        apiIndex.coerceIn(0, VitrinaConstants.UNIT_COUNT - 1)

    fun glbIndexForUnitId(unitId: String?): Int? =
        unitId?.let { apiUnitIdToGlbIndex[it] }

    fun glbNodeNameFor(apiIndex: Int): String =
        orderedGlbNodeNames[glbIndexFor(apiIndex)]

    fun glbNodeNameForUnitId(unitId: String?): String? =
        glbIndexForUnitId(unitId)?.let { orderedGlbNodeNames[it] }

    fun navigationUnitIdFor(glbIndex: Int): String =
        orderedNavigationUnitIds[glbIndexFor(glbIndex)]

    fun bearingFor(glbIndex: Int): Float =
        faces[glbIndexFor(glbIndex)].bearingDegrees

    fun orderUnitsLikeGlb(units: List<com.lasante.tvkiosk.data.VitrinaUnit>): List<com.lasante.tvkiosk.data.VitrinaUnit> {
        if (units.isEmpty()) return units
        val byId = units.associateBy { it.unit.id }
        val ordered = orderedNavigationUnitIds.mapNotNull { id -> byId[id] }
        if (ordered.size == units.size) return ordered
        val seen = ordered.map { it.unit.id }.toSet()
        return ordered + units.filter { it.unit.id !in seen }
    }

    fun frontGlbIndexForRotation(rotationYDegrees: Float): Int {
        var bestIndex = 0
        var bestAbs = Float.MAX_VALUE
        for (face in faces) {
            val world = VitrinaRotation.normalizeDegrees(face.bearingDegrees + rotationYDegrees)
            val dist = minOf(world, 360f - world)
            if (dist < bestAbs) {
                bestAbs = dist
                bestIndex = face.index
            }
        }
        return bestIndex
    }
}
