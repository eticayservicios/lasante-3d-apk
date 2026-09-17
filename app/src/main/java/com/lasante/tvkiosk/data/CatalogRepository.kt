package com.lasante.tvkiosk.data

data class IntroCatalogData(
    val businessUnits: List<BusinessUnit>,
    val vitrinaUnits: List<VitrinaUnit>,
    val vitrinaConfig: VitrinaConfig,
    val screenSaverVideos: List<ScreenSaverVideo>,
    val institutionalVideoUrl: String?,
    /** Catálogo completo (todas las unidades) para el buscador de Intro. */
    val allProducts: List<Product> = emptyList(),
)

data class ProductPage(
    val items: List<Product>,
    val nextCursor: String? = null,
)

interface CatalogRepository {
    suspend fun getIntroCatalogData(): IntroCatalogData
    /**
     * Carga índice slim de productos para el buscador (background tras Intro).
     * No bloquea /home.
     */
    suspend fun ensureProductSearchIndex(): List<Product>
    suspend fun getUnits(): List<BusinessUnit>
    suspend fun getTreatments(unitId: String): List<Treatment>
    suspend fun getProducts(treatmentId: String): List<Product>
    /** Todos los productos de una unidad (todas las clases terapéuticas). */
    suspend fun getProductsForUnit(unitId: String): List<Product>
    /** Primera página (o siguiente) de productos de una unidad — scroll infinito. */
    suspend fun getProductsForUnitPage(
        unitId: String,
        limit: Int = 24,
        cursor: String? = null,
    ): ProductPage
    /**
     * Catálogo global (todas las UN + CT). Usado por VER TODOS desde Intro.
     * No filtra por la unidad activa de la vitrina.
     */
    suspend fun getAllProductsPage(
        limit: Int = 24,
        cursor: String? = null,
    ): ProductPage
    suspend fun getProduct(productId: String): Product?
    suspend fun getVitrinaUnits(): List<VitrinaUnit>
    /** Snapshot en memoria de /home (sin suspend). Null si aún no se cargó. */
    fun cachedVitrinaUnitsOrNull(): List<VitrinaUnit>? = null
    /** Invalida snapshot /home en memoria para forzar el próximo fetch de red. */
    fun invalidateCache() {}
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
    /** Icono del cintillo de producto estrella; si es null, la APK usa el PNG embebido. */
    val starProductIconUrl: String? = null,
)
