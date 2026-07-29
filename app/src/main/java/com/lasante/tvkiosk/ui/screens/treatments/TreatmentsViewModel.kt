package com.lasante.tvkiosk.ui.screens.treatments

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.data.Treatment
import com.lasante.tvkiosk.ui.components.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TreatmentsData(
    val unitName: String,
    val unitDescription: String,
    val treatments: List<Treatment>,
)

/** Cache de clases terapéuticas por unidad (misma Activity que Intro). */
class TreatmentsViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    var uiState by mutableStateOf<UiState<TreatmentsData>>(UiState.Loading)
        private set

    private val cache = mutableMapOf<String, TreatmentsData>()
    private var warmCacheStarted = false

    /** Precarga en background tras el home; no bloquea la vitrina. */
    fun warmCache() {
        if (warmCacheStarted) return
        warmCacheStarted = true
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val units = catalogRepository.getUnits()
                units.forEach { unit ->
                    cache.putIfAbsent(unit.id, buildTreatmentsData(unit.id, units))
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
            uiState = fetchTreatments(unitId)
        }
    }

    private suspend fun fetchTreatments(unitId: String): UiState<TreatmentsData> =
        try {
            val data = withContext(Dispatchers.IO) {
                buildTreatmentsData(unitId)
            }
            cache[unitId] = data
            UiState.Success(data)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error de conexión")
        }

    private suspend fun buildTreatmentsData(
        unitId: String,
        allUnits: List<com.lasante.tvkiosk.data.BusinessUnit>? = null,
    ): TreatmentsData {
        val units = allUnits ?: catalogRepository.getUnits()
        val unit = units.firstOrNull { it.id == unitId }
        val treatments = catalogRepository.getTreatments(unitId)
            .filterNot { it.id.endsWith("-vitrina") }
        return TreatmentsData(
            unitName = unit?.name ?: unitId,
            unitDescription = unit?.description.orEmpty(),
            treatments = treatments,
        )
    }
}
