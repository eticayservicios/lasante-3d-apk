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

    private val cache = mutableMapOf<String, TreatmentsData>()
    private var warmCacheStarted = false
    @Volatile
    private var activeUnitId: String? = null

    fun warmCache() {
        if (warmCacheStarted) return
        warmCacheStarted = true
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val units = catalogRepository.getUnits()
                units.forEach { unit ->
                    cache.putIfAbsent(unit.id, buildTreatmentsShell(unit.id, units))
                }
                // Índice de búsqueda (compartido con Intro); no precargar todos los productos por unidad.
                runCatching { catalogRepository.ensureProductSearchIndex() }
            }
        }
    }

    fun load(unitId: String, forceRefresh: Boolean = false) {
        activeUnitId = unitId
        cache[unitId]?.takeUnless { forceRefresh }?.let { cached ->
            uiState = UiState.Success(cached)
            if (cached.products.isEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching { ensureProductsLoaded(unitId) }
                }
            }
            return
        }

        viewModelScope.launch {
            if (cache[unitId] == null) {
                uiState = UiState.Loading
            }
            uiState = try {
                val shell = withContext(Dispatchers.IO) {
                    buildTreatmentsShell(unitId)
                }
                cache[unitId] = shell
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching { ensureProductsLoaded(unitId) }
                }
                UiState.Success(shell)
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    /** Solo metadatos de clases terapéuticas (lo que ve el grid). */
    private suspend fun buildTreatmentsShell(
        unitId: String,
        allUnits: List<com.lasante.tvkiosk.data.BusinessUnit>? = null,
    ): TreatmentsData {
        val units = allUnits ?: catalogRepository.getUnits()
        val unit = units.firstOrNull { it.id == unitId }
        val treatments = catalogRepository.getTreatments(unitId)
            .filterNot { it.id.endsWith("-vitrina") }
        return TreatmentsData(
            unitName = DisplayTitles.resolve(unit?.name, unitId),
            treatments = treatments,
            products = cache[unitId]?.products.orEmpty(),
        )
    }

    private suspend fun ensureProductsLoaded(unitId: String) {
        val current = cache[unitId] ?: return
        if (current.products.isNotEmpty()) return
        val indexed = catalogRepository.ensureProductSearchIndex()
            .filter { product ->
                product.unidadId.equals(unitId, ignoreCase = true)
            }
        val products = indexed.ifEmpty { catalogRepository.getProductsForUnit(unitId) }
        if (products.isEmpty()) return
        val updated = current.copy(products = products)
        cache[unitId] = updated
        if (activeUnitId == unitId) {
            uiState = UiState.Success(updated)
        }
    }
}
