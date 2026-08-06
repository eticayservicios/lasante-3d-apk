package com.lasante.tvkiosk.ui.screens.intro

/**
 * Orden canónico de unidades en [mobile_draco.glb] y mapeo con IDs del API/backend.
 *
 * Índice 0…4 = pasos de ~72° en el cilindro.
 * Nombre de nodo GLB = texto del cintillo = ID de navegación (1:1).
 *
 * Orden: Genéricos → Primary → PHQ → Specialty → Hospital.
 */
object VitrinaGlbMapping {
    val orderedGlbNodeNames: List<String> = VitrinaConstants.UNIT_GLB_NODE_NAMES

    val orderedNavigationUnitIds: List<String> = listOf(
        "genericos-la-sante",
        "primary-care",
        "phq-consumo",
        "specialty-care",
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

    /** ID de unidad de negocio para navegar al tocar el cintillo de esta cara del GLB. */
    fun navigationUnitIdFor(glbIndex: Int): String =
        orderedNavigationUnitIds[glbIndexFor(glbIndex)]

    /**
     * Reordena unidades del API al orden angular del GLB.
     * Sin esto, `orden` del backend (Specialty antes que PHQ) desincroniza
     * luz / rotación / navegación respecto al cintillo.
     */
    fun orderUnitsLikeGlb(units: List<com.lasante.tvkiosk.data.VitrinaUnit>): List<com.lasante.tvkiosk.data.VitrinaUnit> {
        if (units.isEmpty()) return units
        val byId = units.associateBy { it.unit.id }
        val ordered = orderedNavigationUnitIds.mapNotNull { id -> byId[id] }
        if (ordered.size == units.size) return ordered
        val seen = ordered.map { it.unit.id }.toSet()
        return ordered + units.filter { it.unit.id !in seen }
    }

    /**
     * Índice de la cara cuyo bearing queda más cerca del frente de cámara (+Z)
     * con la rotación Y actual del modelo.
     */
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
