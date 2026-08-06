package com.lasante.tvkiosk.ui.screens.intro

/**
 * Orden canónico de unidades en [mobile_draco.glb] y mapeo con IDs del API/backend.
 *
 * Índice 0…4 = orden angular real del cilindro (~72° por paso):
 * genericos → primary → specialty → phq → hospital.
 *
 * Nombre de nodo = texto UV del cintillo = ID de navegación del mismo índice.
 * (No invertir specialty/phq: el mesh los tiene en ese orden físico.)
 */
object VitrinaGlbMapping {
    val orderedGlbNodeNames: List<String> = VitrinaConstants.UNIT_GLB_NODE_NAMES

    /** ID de negocio por índice visual (mismo orden que nodo GLB / cintillo). */
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
