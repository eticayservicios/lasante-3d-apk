package com.lasante.tvkiosk.ui.screens.intro

/**
 * Orden canónico = API/admin = caras del cilindro.
 *
 * Genéricos → Primary → Specialty → PHQ → Hospital.
 * Nombre de nodo GLB = texto del cintillo = ID de navegación (1:1).
 */
object VitrinaGlbMapping {
    val orderedGlbNodeNames: List<String> = VitrinaConstants.UNIT_GLB_NODE_NAMES

    val orderedNavigationUnitIds: List<String> = listOf(
        "genericos-la-sante",
        "primary-care",
        "specialty-care",
        "phq-consumo",
        "hospital-care",
    )

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

    fun orderUnitsLikeGlb(units: List<com.lasante.tvkiosk.data.VitrinaUnit>): List<com.lasante.tvkiosk.data.VitrinaUnit> {
        if (units.isEmpty()) return units
        val byId = units.associateBy { it.unit.id }
        val ordered = orderedNavigationUnitIds.mapNotNull { id -> byId[id] }
        if (ordered.size == units.size) return ordered
        val seen = ordered.map { it.unit.id }.toSet()
        return ordered + units.filter { it.unit.id !in seen }
    }

    fun frontGlbIndexForRotation(rotationYDegrees: Float): Int {
        val bearings = VitrinaConstants.UNIT_MESH_BEARING_DEGREES
        var bestIndex = 0
        var bestAbs = Float.MAX_VALUE
        for (index in bearings.indices) {
            val world = VitrinaRotation.normalizeDegrees(bearings[index] + rotationYDegrees)
            val dist = minOf(world, 360f - world)
            if (dist < bestAbs) {
                bestAbs = dist
                bestIndex = index
            }
        }
        return bestIndex
    }
}
