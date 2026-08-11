package com.lasante.tvkiosk.data

data class BusinessUnit(
    val unidadId: String,
    val nombre: String,
    val descripcion: String,
    val estado: String = "ACTIVO",
    val orden: Int = 0,
    val media: UnitMedia = UnitMedia(),
    val atributos: Map<String, String> = emptyMap()
) {
    val id: String get() = unidadId
    val name: String get() = nombre
    val description: String get() = descripcion
}

data class UnitMedia(
    val icono: String? = null,
    val portada: String? = null
)

data class Treatment(
    val tratamientoId: String,
    val unidadId: String,
    val nombre: String,
    val descripcion: String,
    val estado: String = "ACTIVO",
    val orden: Int = 0,
    val media: TreatmentMedia = TreatmentMedia(),
    val atributos: Map<String, String> = emptyMap()
) {
    val id: String get() = tratamientoId
    val name: String get() = nombre
    val description: String get() = descripcion
}

data class TreatmentMedia(
    val icono: String? = null,
    val portada: String? = null
)

data class Product(
    val productoId: String,
    val unidadId: String,
    val tratamientoId: String,
    val nombre: String,
    val descripcion: String,
    val estado: String = "ACTIVO",
    val orden: Int = 0,
    val media: ProductMedia = ProductMedia(),
    val atributos: Map<String, String> = emptyMap(),
    /** Cantidad de dosis desde API (ej. "10", "10-20"). */
    val dosisValor: String? = null,
    /** Unidad de dosis desde API (mg, ml, g, mcg, %, UI). */
    val dosisUnidad: String? = null,
) {
    val id: String get() = productoId
    val name: String get() = nombre
    val description: String get() = descripcion
    val modelUrl: String? get() = media.modelo3d.glb
    val audioUrl: String? get() = null // Se agregará cuando el backend defina dónde viene el audio (ej: atributos o media)

    /** Texto listo para UI: "10 mg". Null si falta valor o unidad. */
    val dosisDisplay: String?
        get() {
            val valor = dosisValor?.trim().orEmpty()
            val unidad = dosisUnidad?.trim().orEmpty()
            if (valor.isEmpty() || unidad.isEmpty()) return null
            return "$valor $unidad"
        }
}

data class ProductMedia(
    val imagenes2d: Images2D = Images2D(),
    val modelo3d: Model3D = Model3D()
)

data class Images2D(
    val principal: String? = null,
    val miniatura: String? = null,
    val galeria: List<String> = emptyList()
)

data class Model3D(
    val glb: String? = null,
    val glbFrasco: String? = null,
    val glbAbrircaja: String? = null,
    val usdz: String? = null,
    val vistaPrevia: String? = null
)

data class FeaturedCollection(
    val coleccionId: String,
    val nombre: String,
    val descripcion: String,
    val estado: String,
    val maxItems: Int
)

data class VitrinaUnit(
    val unit: BusinessUnit,
    val products: List<Product>,
    val rotationDegrees: Float = 0f
)
