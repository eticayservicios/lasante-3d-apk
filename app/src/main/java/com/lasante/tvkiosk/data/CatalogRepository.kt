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
    suspend fun getProduct(productId: String): Product?
    suspend fun getFeaturedProducts(): List<Product>
    suspend fun getVitrinaUnits(): List<VitrinaUnit>
    suspend fun getVitrinaConfig(): VitrinaConfig
    suspend fun getScreenSaverVideos(): List<ScreenSaverVideo>
    suspend fun getInstitutionalVideoUrl(): String?
    suspend fun getAllProducts(): List<Product>
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
    val tipo: String?
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
