package com.lasante.tvkiosk.ui.screens.intro

/**
 * Orden canónico de unidades en [mobile_draco.glb] y mapeo con IDs del API/backend.
 *
 * Índice 0…4 = pasos de ~72° en el cilindro.
 * Los nodos `specialty_care` / `phq_consumo` tienen el **nombre cruzado** respecto
 * al texto del cintillo (atlas con paneles PHQ/Specialty intercambiados para
 * respetar el orden visual: … → PHQ → Specialty →…).
 *
 * [orderedGlbNodeNames] = mesh a rotar/encender en cada paso.
 * [orderedNavigationUnitIds] = ID de negocio según el **texto visible**.
 */
object VitrinaGlbMapping {
    val orderedGlbNodeNames: List<String> = VitrinaConstants.UNIT_GLB_NODE_NAMES

    /** ID de negocio por índice visual (texto del cintillo, no el nombre del nodo). */
    val orderedNavigationUnitIds: List<String> = listOf(
        "genericos-la-sante",
        "primary-care",
        "phq-consumo", // cara index 2: nodo specialty_care, texto PHQ
        "specialty-care", // cara index 3: nodo phq_consumo, texto Specialty
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
