package com.lasante.tvkiosk.ui.screens.treatments

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.data.DisplayTitles
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.data.Treatment
import com.lasante.tvkiosk.ui.components.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class TreatmentsData(
    val unitName: String,
    val treatments: List<Treatment>,
    /** Catálogo de la unidad para el buscador; puede llegar vacío y rellenarse luego. */
    val products: List<Product>,
)

/** Cache de clases terapéuticas por unidad (misma Activity que Intro). */
class TreatmentsViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    var uiState by mutableStateOf<UiState<TreatmentsData>>(UiState.Loading)
        private set

    private val cache = ConcurrentHashMap<String, TreatmentsData>()
    private var warmCacheStarted = false

    fun warmCache() {
        if (warmCacheStarted) return
        warmCacheStarted = true
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val units = catalogRepository.getUnits()
                val productIndex = runCatching {
                    catalogRepository.ensureProductSearchIndex()
                }.getOrNull()
                units.forEach { unit ->
                    val data = buildTreatmentsData(unit.id, units, productIndex)
                    cache.putIfAbsent(unit.id, data)
                }
            }
        }
    }

    fun load(unitId: String, forceRefresh: Boolean = false) {
        cache[unitId]?.takeUnless { forceRefresh }?.let { cached ->
            uiState = UiState.Success(cached)
            return
        }

        viewModelScope.launch {
            if (cache[unitId] == null) {
                uiState = UiState.Loading
            }
            uiState = try {
                val data = withContext(Dispatchers.IO) {
                    buildTreatmentsData(unitId)
                }
                cache[unitId] = data
                UiState.Success(data)
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    /** Solo metadatos de clases terapéuticas (lo que ve el grid). */
    private suspend fun buildTreatmentsData(
        unitId: String,
        allUnits: List<com.lasante.tvkiosk.data.BusinessUnit>? = null,
        productIndex: List<Product>? = null,
    ): TreatmentsData {
        val units = allUnits ?: catalogRepository.getUnits()
        val unit = units.firstOrNull { it.id == unitId }
        val treatments = catalogRepository.getTreatments(unitId)
            .filterNot { it.id.endsWith("-vitrina") }
        val shell = TreatmentsData(
            unitName = DisplayTitles.resolve(unit?.name, unitId),
            treatments = treatments,
            products = emptyList(),
        )
        val resolvedProductIndex = productIndex ?: runCatching {
            catalogRepository.ensureProductSearchIndex()
        }.getOrNull()
        return shell.filterEmptyTreatments(unitId, resolvedProductIndex)
    }

    private fun String.catalogDisplayKey(): String =
        Normalizer.normalize(trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "")

    /**
     * Oculta CT sin productos usando un único índice global. Si el índice falla o
     * está vacío, conserva el catálogo completo: la red nunca debe borrar CT visibles.
     */
    private fun TreatmentsData.filterEmptyTreatments(
        unitId: String,
        productIndex: List<Product>?,
    ): TreatmentsData {
        val safeIndex = productIndex?.takeIf { it.isNotEmpty() } ?: return this
        val unitKeys = catalogRepository.matchingUnitIds(unitId)
            .map { it.lowercase(Locale.ROOT) }
            .toSet()
        val productsForUnit = safeIndex.filter { product ->
            product.unidadId.lowercase(Locale.ROOT) in unitKeys
        }
        val treatmentIdsWithProducts = productsForUnit
            .map { it.tratamientoId }
            .filter { it.isNotBlank() }
            .map { it.lowercase(Locale.ROOT) }
            .toSet()
        return copy(
            // Primero conservamos solo las CT con productos. Luego resolvemos nombres
            // duplicados para no descartar una CT válida cuyo duplicado vacío llegó antes.
            treatments = treatments
                .filter { it.id.lowercase(Locale.ROOT) in treatmentIdsWithProducts }
                .distinctBy { it.name.catalogDisplayKey() },
            products = productsForUnit,
        )
    }
}
