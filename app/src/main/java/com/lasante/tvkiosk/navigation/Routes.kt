package com.lasante.tvkiosk.navigation

object Routes {
    const val INTRO = "intro"
    const val TREATMENTS = "treatments"
    const val PRODUCTS = "products"
}

object Args {
    const val UNIT_ID = "unitId"
    const val TREATMENT_ID = "treatmentId"
    const val PRODUCT_ID = "productId"
    /** Ver todos los productos de la unidad (sin filtrar por clase terapéutica). */
    const val ALL_TREATMENTS_ID = "all"
    /**
     * Productos estrella de la unidad activa en vitrina.
     * Reusa la ruta `products/{unitId}/{treatmentId}` sin pantalla duplicada.
     */
    const val STAR_PRODUCTS_ID = "estrellas"
}
