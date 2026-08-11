package com.lasante.tvkiosk.data.remote

import com.lasante.tvkiosk.data.*
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RetrofitCatalogRepository(
    private val api: CatalogApiService = RetrofitClient.catalogApi
) : CatalogRepository {

    private var homeSnapshot: HomeSnapshot? = null
    private var homeCacheTime: Long = 0
    /** Snapshot /home en memoria (kiosco). Antes 5 min; ahora 24 h. [invalidateCache] fuerza refresh. */
    private val CACHE_TTL_MS = 24L * 60 * 60 * 1000

    private data class BusinessUnitAlias(
        val fallbackId: String,
        val displayName: String,
        val aliases: Set<String>,
    )

    private data class HomeSnapshot(
        val dto: HomeDto,
        val businessUnits: List<BusinessUnit>,
        val vitrinaUnits: List<VitrinaUnit>,
        val vitrinaConfig: VitrinaConfig,
        val screenSaverVideos: List<ScreenSaverVideo>,
        val institutionalVideoUrl: String?,
        val catalogEntries: List<CatalogEntry>,
        val productsByKey: Map<String, CatalogEntry>,
    )

    private data class CatalogEntry(
        val unitId: String,
        val treatmentId: String,
        val product: ProductoDto,
    )

    private val businessUnitAliases = listOf(
        BusinessUnitAlias(
            fallbackId = "genericos-la-sante",
            displayName = "Genéricos La Santé",
            aliases = setOf(
                "genericos-la-sante",
                "genericos la sante",
                "medicina-general",
                "medicina general",
                "genericos",
                "genéricos",
                "genéricos la santé",
            ),
        ),
        BusinessUnitAlias(
            fallbackId = "primary-care",
            displayName = "Primary Care",
            aliases = setOf("primary", "primary-care", "primary care"),
        ),
        BusinessUnitAlias(
            fallbackId = "specialty-care",
            displayName = "Specialty Care",
            aliases = setOf("specialty", "specialty-care", "specialty care"),
        ),
        BusinessUnitAlias(
            fallbackId = "phq-consumo",
            displayName = "PHQ Consumo",
            aliases = setOf("phq", "phq-consumo", "phq consumo", "consumo"),
        ),
        BusinessUnitAlias(
            fallbackId = "hospital-care",
            displayName = "Hospital Care",
            aliases = setOf("hospital", "hospital-care", "hospital care"),
        ),
    )

    fun invalidateCache() {
        homeSnapshot = null
        homeCacheTime = 0
    }

    private suspend fun getSnapshot(): HomeSnapshot = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (homeSnapshot == null || (now - homeCacheTime) > CACHE_TTL_MS) {
            val dto = api.getHome()
            homeSnapshot = dto.toSnapshot()
            homeCacheTime = now
            android.util.Log.d(
                "RetrofitCatalogRepository",
                "home loaded: unidades=${dto.unidades.size}, vitrinaUnits=${dto.vitrina?.units?.size ?: -1}, " +
                    "videos=${dto.vitrina?.videos?.screenSaver?.items?.size ?: -1}",
            )
        }
        homeSnapshot!!
    }

    private fun String.normalizedKey(): String {
        val withoutAccents = Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return withoutAccents
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }

    private fun BusinessUnitAlias.matches(unit: UnidadNegocioWrapperDto): Boolean {
        val candidateKeys = listOf(unit.id, unit.nombre.orEmpty())
            .map { it.normalizedKey() }
            .filter { it.isNotBlank() }
        val aliasKeys = aliases.map { it.normalizedKey() }

        return aliasKeys.any { alias ->
            candidateKeys.any { candidate ->
                candidate == alias || candidate.contains(alias) || alias.contains(candidate)
            }
        }
    }

    private fun HomeDto.findBusinessUnitByIdOrAlias(unitId: String): UnidadNegocioWrapperDto? {
        val requestedKey = unitId.normalizedKey()
        val exactUnit = unidades.firstOrNull { it.id.normalizedKey() == requestedKey }
        if (exactUnit != null) return exactUnit

        val canonical = businessUnitAliases.firstOrNull { target ->
            target.fallbackId.normalizedKey() == requestedKey ||
                target.aliases.any { it.normalizedKey() == requestedKey }
        }

        return canonical?.let { target ->
            unidades.firstOrNull { target.matches(it) }
        }
    }

    private fun ItemMediaDto?.toProductMedia(): ProductMedia {
        val principalUrl = this?.imagenes2d?.principal?.takeIf { it.isNotBlank() }
            ?: this?.principal?.takeIf { it.isNotBlank() }
        val miniaturaUrl = this?.imagenes2d?.miniatura?.takeIf { it.isNotBlank() }
            ?: this?.miniatura?.takeIf { it.isNotBlank() }
            ?: principalUrl
        val gallery = this?.imagenes2d?.galeria?.takeIf { it.isNotEmpty() }
            ?: this?.galeria
            ?: emptyList()
        val modern3d = this?.modelo3d
        val legacy3d = this?.modelo3dLegacy
        val glbUrl = modern3d?.glb?.takeIf { it.isNotBlank() }
            ?: legacy3d?.glb?.takeIf { it.isNotBlank() }
        val glbFrasco = modern3d?.glbFrasco?.takeIf { it.isNotBlank() }
            ?: modern3d?.glbFrascoLegacy?.takeIf { it.isNotBlank() }
            ?: legacy3d?.glbFrasco?.takeIf { it.isNotBlank() }
        val glbAbrircaja = modern3d?.glbAbrircaja?.takeIf { it.isNotBlank() }
            ?: modern3d?.glbAbrircajaLegacy?.takeIf { it.isNotBlank() }
            ?: legacy3d?.glbAbrircaja?.takeIf { it.isNotBlank() }
        val previewUrl = modern3d?.vistaPrevia?.takeIf { it.isNotBlank() }
            ?: modern3d?.preview?.takeIf { it.isNotBlank() }
            ?: legacy3d?.preview?.takeIf { it.isNotBlank() }
            ?: miniaturaUrl

        return ProductMedia(
            imagenes2d = Images2D(principal = principalUrl, miniatura = miniaturaUrl, galeria = gallery),
            modelo3d = Model3D(
                glb = glbUrl,
                glbFrasco = glbFrasco,
                glbAbrircaja = glbAbrircaja,
                usdz = modern3d?.usdz ?: legacy3d?.usdz,
                vistaPrevia = previewUrl
            )
        )
    }

    private fun ProductoDto.toProduct(unidadId: String, tratamientoId: String): Product {
        return Product(
            productoId    = id,
            unidadId      = unidadId,
            tratamientoId = tratamientoId,
            nombre        = nombre ?: "",
            descripcion   = descripcion ?: "",
            estado        = "ACTIVO",
            orden         = 0,
            media         = media.toProductMedia(),
            atributos = emptyMap(),
            dosisValor = dosisValor,
            dosisUnidad = dosisUnidad,
        )
    }

    private fun ScreenSaverVideoDto.toDomain(orderFallback: Int): ScreenSaverVideo? {
        val resolvedUrl = url?.trim().orEmpty()
        if (resolvedUrl.isBlank()) return null
        return ScreenSaverVideo(
            id = id?.takeIf { it.isNotBlank() } ?: "screen-saver-$orderFallback",
            title = title?.takeIf { it.isNotBlank() } ?: "Video ${orderFallback + 1}",
            url = resolvedUrl,
            enabled = enabled,
            order = order?.toIntOrNull() ?: orderFallback
        )
    }

    private fun VitrinaProductDto.toProduct(
        defaultUnitId: String,
        defaultTreatmentId: String = "",
        defaultOrder: Int = 0,
        vitrinaSlot: Int? = null,
        modalEnabled: Boolean = true,
    ): Product {
        val resolvedSlot = vitrinaSlot?.takeIf { it > 0 }
            ?: slot?.takeIf { it > 0 }
            ?: orden?.takeIf { it > 0 }
            ?: defaultOrder.takeIf { it > 0 }
            ?: 1
        return Product(
            productoId    = id,
            unidadId      = unidadId ?: defaultUnitId,
            tratamientoId = tratamientoId ?: defaultTreatmentId,
            nombre        = nombre ?: "",
            descripcion   = descripcion ?: "",
            estado        = estado ?: "ACTIVO",
            orden         = orden ?: resolvedSlot,
            media         = media.toProductMedia(),
            atributos     = mapOf(
                "slot" to resolvedSlot.toString(),
                "modalEnabled" to modalEnabled.toString(),
            ),
            dosisValor = dosisValor,
            dosisUnidad = dosisUnidad,
        )
    }

    private fun HomeDto.toSnapshot(): HomeSnapshot {
        val catalogEntries = unidades.flatMap { unit ->
            unit.tratamientos.flatMap { treatment ->
                treatment.productos.map { product ->
                    CatalogEntry(unit.id, treatment.id, product)
                }
            }
        }
        val productsByKey = catalogEntries.flatMap { entry ->
            val keys = buildList {
                add(entry.product.id.lowercase())
                entry.product.slug?.lowercase()?.takeIf { it.isNotBlank() }?.let { add(it) }
            }
            keys.map { key -> key to entry }
        }.toMap()

        return HomeSnapshot(
            dto = this,
            businessUnits = mapBusinessUnits(),
            vitrinaUnits = mapVitrinaUnits(),
            vitrinaConfig = mapVitrinaConfig(),
            screenSaverVideos = mapScreenSaverVideos(),
            institutionalVideoUrl = videoInstitucional?.trim()?.takeIf { it.isNotBlank() },
            catalogEntries = catalogEntries,
            productsByKey = productsByKey,
        )
    }

    private fun HomeDto.mapBusinessUnits(): List<BusinessUnit> =
        unidades.map { u ->
            BusinessUnit(
                unidadId    = u.id,
                nombre      = DisplayTitles.resolve(u.nombre, u.id),
                descripcion = u.descripcion ?: "",
                estado      = "ACTIVO",
                orden       = 0,
                media       = UnitMedia(),
                atributos   = emptyMap()
            )
        }

    private fun HomeDto.mapVitrinaUnits(): List<VitrinaUnit> {
        val vitrinaUnits = vitrina?.units.orEmpty()
        android.util.Log.d(
            "RetrofitCatalogRepository",
            "mapping vitrina: enabled=${vitrina?.enabled}, incomingUnits=${vitrinaUnits.size}",
        )
        if (vitrina?.enabled == false || vitrinaUnits.isEmpty()) {
            android.util.Log.w(
                "RetrofitCatalogRepository",
                "vitrina config missing or disabled — returning empty (no fallback)",
            )
            return emptyList()
        }

        // Orden = API/admin = cilindro: Specialty antes que PHQ.
        val glbOrder = listOf(
            "genericos-la-sante",
            "primary-care",
            "specialty-care",
            "phq-consumo",
            "hospital-care",
            "medicina-general",
        )
        return vitrinaUnits
            .sortedBy { unit ->
                val unitId = unit.unitId ?: unit.id
                val idx = glbOrder.indexOf(unitId)
                if (idx >= 0) idx else 1000 + (unit.orden ?: Int.MAX_VALUE)
            }
            .distinctBy { it.unitId ?: it.id }
            .take(vitrina?.maxUnits ?: 5)
            .mapIndexed { index, unit ->
                val unitId = unit.unitId ?: unit.id
                val resolvedProducts = unit.slots.orEmpty()
                    .filter { it.visible && it.product != null }
                    .sortedBy { it.order ?: it.slot }
                    .mapNotNull { slot ->
                        slot.product?.toProduct(
                            defaultUnitId = unitId,
                            defaultTreatmentId = slot.treatmentId.orEmpty(),
                            defaultOrder = slot.order ?: slot.slot,
                            vitrinaSlot = slot.slot.takeIf { it > 0 },
                            modalEnabled = slot.modalEnabled,
                        )
                    }
                android.util.Log.i(
                    "RetrofitCatalogRepository",
                    "unit[$index]=$unitId slots=${unit.slots.orEmpty().size} " +
                        "visible=${unit.slots.orEmpty().count { it.visible && it.product != null }} " +
                        "resolved=${resolvedProducts.size} " +
                        "slotMap=${resolvedProducts.map { "${it.productoId}@slot${it.atributos["slot"]}" }} " +
                        "glbs=${resolvedProducts.map { it.media.modelo3d.glbFrasco ?: it.media.modelo3d.glb }}",
                )

                VitrinaUnit(
                    unit = BusinessUnit(
                        unidadId = unitId,
                        nombre = DisplayTitles.resolve(unit.nombre, unitId),
                        descripcion = unit.descripcion ?: "",
                        media = UnitMedia(icono = unit.icono)
                    ),
                    products = resolvedProducts,
                    rotationDegrees = ((vitrina?.rotationStepDegrees ?: 72) * index).toFloat()
                )
            }
    }

    private fun HomeDto.mapVitrinaConfig(): VitrinaConfig {
        val vitrinaMeta = vitrina
        return VitrinaConfig(
            autoRotateAfterMs = vitrinaMeta?.autoRotateAfterMs ?: 120_000L,
            screenSaverAfterMs = vitrinaMeta?.screenSaverAfterMs ?: 180_000L,
            screenSaverPlaylistEnabled = vitrinaMeta?.videos?.screenSaver?.enabled == true,
        )
    }

    private fun HomeDto.mapScreenSaverVideos(): List<ScreenSaverVideo> {
        val playlist = vitrina?.videos?.screenSaver
        if (playlist?.enabled != true) return emptyList()
        return ScreenSaverVideoPolicy.filterPlaylist(
            playlist.items
                .mapIndexedNotNull { index, item -> item.toDomain(index) }
                .filter { it.url.isNotBlank() }
                .sortedBy { it.order }
                .take(5),
        )
    }

    override suspend fun getIntroCatalogData(): IntroCatalogData {
        val snapshot = getSnapshot()
        return IntroCatalogData(
            businessUnits = snapshot.businessUnits,
            vitrinaUnits = snapshot.vitrinaUnits,
            vitrinaConfig = snapshot.vitrinaConfig,
            screenSaverVideos = snapshot.screenSaverVideos,
            institutionalVideoUrl = snapshot.institutionalVideoUrl,
        )
    }

    override suspend fun getUnits(): List<BusinessUnit> =
        runCatching { getSnapshot().businessUnits }.getOrElse { emptyList() }

    override suspend fun getTreatments(unitId: String): List<Treatment> =
        runCatching {
            getSnapshot().dto.findBusinessUnitByIdOrAlias(unitId)
                ?.tratamientos
                ?.map { t ->
                    Treatment(
                        tratamientoId = t.id,
                        unidadId      = unitId,
                        nombre        = DisplayTitles.resolve(t.nombre, t.id),
                        descripcion   = t.descripcion ?: "",
                        estado        = "ACTIVO",
                        orden         = 0,
                        media         = TreatmentMedia(icono = t.icono, portada = null),
                        atributos     = emptyMap()
                    )
                } ?: emptyList()
        }.getOrElse { emptyList() }

    override suspend fun getProducts(treatmentId: String): List<Product> =
        runCatching {
            getSnapshot().catalogEntries
                .filter { it.treatmentId == treatmentId }
                .map { (unitId, treatmentIdValue, product) ->
                    product.toProduct(unitId, treatmentIdValue)
                }
        }.getOrElse { emptyList() }

    override suspend fun getProductsForUnit(unitId: String): List<Product> =
        runCatching {
            val snapshot = getSnapshot()
            val resolvedId = snapshot.dto.findBusinessUnitByIdOrAlias(unitId)?.id ?: unitId
            snapshot.catalogEntries
                .asSequence()
                .filter { it.unitId == resolvedId || it.unitId == unitId }
                .map { (u, t, product) -> product.toProduct(u, t) }
                .distinctBy { it.productoId }
                .toList()
        }.getOrElse { emptyList() }

    override suspend fun getProduct(productId: String): Product? =
        runCatching {
            getSnapshot().catalogEntries
                .firstOrNull { it.product.id == productId }
                ?.let { (unitId, treatmentId, product) -> product.toProduct(unitId, treatmentId) }
        }.getOrNull()

    override suspend fun getFeaturedProducts(): List<Product> =
        runCatching {
            val snapshot = getSnapshot()
            snapshot.dto.itemsDestacados
                .flatMap { wrapper -> wrapper.items }
                .sortedBy { it.orden?.toIntOrNull() ?: 0 }
                .mapNotNull { item ->
                    val entry = snapshot.productsByKey[item.id.lowercase()]
                    if (entry != null) {
                        entry.product.toProduct(entry.unitId, entry.treatmentId)
                    } else {
                        android.util.Log.w(
                            "RetrofitCatalogRepository",
                            "featured item skipped (no catalog match): id=${item.id} name=${item.nombre}",
                        )
                        null
                    }
                }
        }.getOrElse { emptyList() }

    override suspend fun getVitrinaUnits(): List<VitrinaUnit> =
        runCatching { getSnapshot().vitrinaUnits }.getOrElse {
            android.util.Log.e("RetrofitCatalogRepository", "getVitrinaUnits failed: ${it.message}", it)
            emptyList()
        }

    override fun cachedVitrinaUnitsOrNull(): List<VitrinaUnit>? = homeSnapshot?.vitrinaUnits

    override suspend fun getVitrinaConfig(): VitrinaConfig =
        runCatching { getSnapshot().vitrinaConfig }.getOrElse { VitrinaConfig() }

    override suspend fun getScreenSaverVideos(): List<ScreenSaverVideo> =
        runCatching { getSnapshot().screenSaverVideos }.getOrElse { emptyList() }

    override suspend fun getInstitutionalVideoUrl(): String? =
        runCatching { getSnapshot().institutionalVideoUrl }.getOrNull()

    override suspend fun getAllProducts(): List<Product> =
        runCatching {
            getSnapshot().catalogEntries.map { (unitId, treatmentId, product) ->
                product.toProduct(unitId, treatmentId)
            }
        }.getOrElse { emptyList() }

    override suspend fun search(query: String, type: String?): SearchResult =
        runCatching {
            api.search(query, type).toDomain()
        }.getOrElse {
            SearchResult(query = query, count = 0, items = emptyList())
        }
}
