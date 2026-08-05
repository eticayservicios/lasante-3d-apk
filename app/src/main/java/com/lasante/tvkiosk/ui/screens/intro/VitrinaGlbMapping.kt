package com.lasante.tvkiosk.ui.screens.intro

/**
 * Orden canónico de unidades en [mobile_draco.glb] y mapeo con IDs del API/backend.
 *
 * Índice 0…4 ↔ cara visual del cintillo ↔ ID de navegación.
 * Los nodos GLB de specialty/phq están cruzados a propósito en
 * [VitrinaConstants.UNIT_GLB_NODE_NAMES] (texto UV ≠ nombre de nodo).
 */
object VitrinaGlbMapping {
    val orderedGlbNodeNames: List<String> = VitrinaConstants.UNIT_GLB_NODE_NAMES

    /** IDs de navegación alineados al índice visual 0…4 (no al nombre crudo del nodo). */
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

    /** ID de unidad de negocio para navegar al tocar el cintillo de esta cara del GLB. */
    fun navigationUnitIdFor(glbIndex: Int): String =
        orderedNavigationUnitIds[glbIndexFor(glbIndex)]
}
