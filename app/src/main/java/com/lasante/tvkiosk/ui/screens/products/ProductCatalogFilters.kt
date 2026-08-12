package com.lasante.tvkiosk.ui.screens.products

import com.lasante.tvkiosk.data.Product

/** Forma farmacéutica para filtros de clase terapéutica (valores API/admin). */
enum class FormaFarmaceutica(val apiValue: String, val label: String) {
    COMPRIMIDOS("comprimidos", "Comprimidos"),
    CAPSULAS("capsulas", "Cápsulas"),
    SUSPENSION("suspension", "Suspensión"),
    OTROS("otros", "Otros"),
    ;

    companion object {
        fun fromApi(raw: String?): FormaFarmaceutica? {
            val normalized = raw?.trim()?.lowercase().orEmpty()
            if (normalized.isEmpty()) return null
            return entries.firstOrNull { it.apiValue == normalized }
        }
    }
}

/**
 * Filtros combinados (AND) para pantalla de clase terapéutica:
 * presentación + productos estrella de la unidad.
 */
data class TherapeuticClassCatalogFilter(
    val formas: Set<FormaFarmaceutica> = emptySet(),
    val starProductsOnly: Boolean = false,
) {
    val isActive: Boolean get() = starProductsOnly || formas.isNotEmpty()
}

fun applyTherapeuticClassCatalogFilter(
    products: List<Product>,
    starProductIds: Set<String>,
    filter: TherapeuticClassCatalogFilter,
): List<Product> {
    if (!filter.isActive) return products

    return products.filter { product ->
        val matchesStar = !filter.starProductsOnly || product.productoId in starProductIds
        val matchesForma = when {
            filter.formas.isEmpty() -> true
            else -> {
                val forma = FormaFarmaceutica.fromApi(product.formaFarmaceutica)
                when {
                    forma != null -> forma in filter.formas
                    FormaFarmaceutica.OTROS in filter.formas -> true
                    else -> false
                }
            }
        }
        matchesStar && matchesForma
    }
}
