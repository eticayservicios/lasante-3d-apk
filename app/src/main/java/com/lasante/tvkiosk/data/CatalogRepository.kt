package com.lasante.tvkiosk.data

data class IntroCatalogData(
    val businessUnits: List<BusinessUnit>,
    val vitrinaUnits: List<VitrinaUnit>,
    val vitrinaConfig: VitrinaConfig,
    val screenSaverVideos: List<ScreenSaverVideo>,
    val institutionalVideoUrl: String?,
)

interface CatalogRepository {
    suspend fun getIntroCatalogData(): IntroCatalogData
    suspend fun getUnits(): List<BusinessUnit>
    suspend fun getTreatments(unitId: String): List<Treatment>
    suspend fun getProducts(treatmentId: String): List<Product>
    /** Todos los productos de una unidad (todas las clases terapéuticas). */
    suspend fun getProductsForUnit(unitId: String): List<Product>
    suspend fun getProduct(productId: String): Product?
    suspend fun getVitrinaUnits(): List<VitrinaUnit>
    /** Snapshot en memoria de /home (sin suspend). Null si aún no se cargó. */
    fun cachedVitrinaUnitsOrNull(): List<VitrinaUnit>? = null
    suspend fun getVitrinaConfig(): VitrinaConfig
    suspend fun getScreenSaverVideos(): List<ScreenSaverVideo>
    suspend fun getInstitutionalVideoUrl(): String?
    suspend fun search(query: String, type: String? = null): SearchResult
}

data class SearchResult(
    val query: String,
    val count: Int,
    val items: List<SearchItem>
)

data class SearchItem(
    val id: String,
    val nombre: String,
    val descripcion: String?,
    val tipo: String?,
    val dosisValor: String? = null,
    val dosisUnidad: String? = null,
)


data class ScreenSaverVideo(
    val id: String,
    val title: String,
    val url: String,
    val enabled: Boolean = true,
    val order: Int = 0
)


data class VitrinaConfig(
    val autoRotateAfterMs: Long = 120_000L,
    val screenSaverAfterMs: Long = 180_000L,
    val screenSaverPlaylistEnabled: Boolean = false,
)
