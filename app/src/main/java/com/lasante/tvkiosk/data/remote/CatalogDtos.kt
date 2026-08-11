package com.lasante.tvkiosk.data.remote

import com.google.gson.annotations.SerializedName

// ── Home ───────────────────────────────────────────────────────────
data class HomeDto(
    @SerializedName("unidades")             val unidades: List<UnidadNegocioWrapperDto> = emptyList(),
    @SerializedName("itemsDestacados")      val itemsDestacados: List<ItemDestacadoWrapperDto> = emptyList(),
    @SerializedName("coleccionDestacados")  val coleccionDestacados: ColeccionDestacadosDto? = null,
    @SerializedName("vitrina")              val vitrina: VitrinaDto? = null,
    @SerializedName("videoInstitucional")   val videoInstitucional: String? = null
)

data class VitrinaDto(
    @SerializedName("name")                val name: String? = null,
    @SerializedName("enabled")             val enabled: Boolean = true,
    @SerializedName("rotationStepDegrees") val rotationStepDegrees: Int = 72,
    @SerializedName("maxUnits")            val maxUnits: Int = 5,
    @SerializedName("slotsPerUnit")        val slotsPerUnit: Int = 4,
    @SerializedName("autoRotateAfterMs")   val autoRotateAfterMs: Long? = null,
    @SerializedName("screenSaverAfterMs")  val screenSaverAfterMs: Long? = null,
    @SerializedName("videos")              val videos: VitrinaVideosDto? = null,
    @SerializedName("units")               val units: List<VitrinaUnitDto> = emptyList()
)


data class VitrinaVideosDto(
    @SerializedName("screenSaver") val screenSaver: ScreenSaverPlaylistDto? = null
)

data class ScreenSaverPlaylistDto(
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("items")   val items: List<ScreenSaverVideoDto> = emptyList()
)

data class ScreenSaverVideoDto(
    @SerializedName("id")      val id: String? = null,
    @SerializedName("title")   val title: String? = null,
    @SerializedName("url")     val url: String? = null,
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("order")   val order: String? = null
)

data class VitrinaUnitDto(
    @SerializedName("id")          val id: String,
    @SerializedName("unitId")      val unitId: String? = null,
    @SerializedName("nombre")      val nombre: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("icono")       val icono: String? = null,
    @SerializedName("orden")       val orden: Int? = null,
    @SerializedName("slots")       val slots: List<VitrinaSlotDto>? = null,
    @SerializedName("productos")   val productos: List<VitrinaProductDto>? = null
)

data class VitrinaSlotDto(
    @SerializedName("id")           val id: String? = null,
    @SerializedName("slot")         val slot: Int = 0,
    @SerializedName("order")        val order: Int? = null,
    @SerializedName("visible")      val visible: Boolean = true,
    @SerializedName("modalEnabled") val modalEnabled: Boolean = true,
    @SerializedName("unitId")       val unitId: String? = null,
    @SerializedName("treatmentId")  val treatmentId: String? = null,
    @SerializedName("productId")    val productId: String? = null,
    @SerializedName("product")      val product: VitrinaProductDto? = null
)

data class VitrinaProductDto(
    @SerializedName("id")            val id: String,
    @SerializedName("slug")          val slug: String? = null,
    @SerializedName("nombre")        val nombre: String? = null,
    @SerializedName("descripcion")   val descripcion: String? = null,
    @SerializedName("dosisValor")    val dosisValor: String? = null,
    @SerializedName("dosisUnidad")   val dosisUnidad: String? = null,
    @SerializedName("estado")        val estado: String? = "ACTIVO",
    @SerializedName("orden")         val orden: Int? = null,
    @SerializedName("slot")          val slot: Int? = null,
    @SerializedName("unidadId")      val unidadId: String? = null,
    @SerializedName("tratamientoId") val tratamientoId: String? = null,
    @SerializedName("media")         val media: ItemMediaDto? = null
)

data class ColeccionDestacadosDto(
    @SerializedName("id")     val id: String? = null,
    @SerializedName("nombre") val nombre: String? = null
)

// ── Unidades ───────────────────────────────────────────────────────
data class UnidadNegocioWrapperDto(
    @SerializedName("id")           val id: String,
    @SerializedName("nombre")       val nombre: String? = null,
    @SerializedName("descripcion")  val descripcion: String? = null,
    @SerializedName("icono")        val icono: String? = null,
    @SerializedName("tratamientos") val tratamientos: List<TratamientoDto> = emptyList()
)

data class TratamientoDto(
    @SerializedName("id")          val id: String,
    @SerializedName("nombre")      val nombre: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("icono")       val icono: String? = null,
    @SerializedName("productos")   val productos: List<ProductoDto> = emptyList()
)

data class ProductoDto(
    @SerializedName("id")          val id: String,
    @SerializedName("slug")        val slug: String? = null,
    @SerializedName("nombre")      val nombre: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("dosisValor")  val dosisValor: String? = null,
    @SerializedName("dosisUnidad") val dosisUnidad: String? = null,
    @SerializedName("media")       val media: ItemMediaDto? = null
)

// ── Items Destacados ───────────────────────────────────────────────
data class ItemDestacadoWrapperDto(
    @SerializedName("id")    val id: String,
    @SerializedName("items") val items: List<ItemDestacadoDto> = emptyList()
)

data class ItemDestacadoDto(
    @SerializedName("id")     val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("estado") val estado: String? = "ACTIVO",
    @SerializedName("orden")  val orden: String? = null,
    @SerializedName("media")  val media: ItemMediaDto? = null
)

// ── Media ──────────────────────────────────────────────────────────
data class ItemMediaDto(
    @SerializedName("principal") val principal: String? = null,
    @SerializedName("miniatura") val miniatura: String? = null,
    @SerializedName("galeria")   val galeria: List<String> = emptyList(),
    @SerializedName("3d")        val modelo3dLegacy: Simple3DDto? = null,
    @SerializedName("imagenes2d") val imagenes2d: Images2DDto? = null,
    @SerializedName("modelo3d")   val modelo3d: Model3DDto? = null
)

data class Simple3DDto(
    @SerializedName("preview")      val preview: String? = null,
    @SerializedName("glb")          val glb: String? = null,
    @SerializedName("glb_frasco")   val glbFrasco: String? = null,
    @SerializedName("glb_abrircaja") val glbAbrircaja: String? = null,
    @SerializedName("usdz")         val usdz: String? = null
)

data class Images2DDto(
    @SerializedName("principal") val principal: String? = null,
    @SerializedName("miniatura") val miniatura: String? = null,
    @SerializedName("galeria")   val galeria: List<String> = emptyList()
)

data class Model3DDto(
    @SerializedName("preview")       val preview: String? = null,
    @SerializedName("vistaPrevia")   val vistaPrevia: String? = null,
    @SerializedName("glb")           val glb: String? = null,
    @SerializedName("glb_frasco")    val glbFrascoLegacy: String? = null,
    @SerializedName("glbFrasco")     val glbFrasco: String? = null,
    @SerializedName("glb_abrircaja") val glbAbrircajaLegacy: String? = null,
    @SerializedName("glbAbrircaja")  val glbAbrircaja: String? = null,
    @SerializedName("usdz")          val usdz: String? = null
)

// ── Búsqueda ──────────────────────────────────────────────────────
data class SearchResultDto(
    @SerializedName("query")  val query: String = "",
    @SerializedName("count")  val count: Int = 0,
    @SerializedName("items")  val items: List<SearchItemDto> = emptyList()
)

data class SearchItemDto(
    @SerializedName("id")          val id: String,
    @SerializedName("nombre")      val nombre: String,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("dosisValor")  val dosisValor: String? = null,
    @SerializedName("dosisUnidad") val dosisUnidad: String? = null,
    @SerializedName("tipo")        val tipo: String? = null
)
