package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.data.IntroCatalogData
import com.lasante.tvkiosk.ui.components.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Conserva catálogo de Intro mientras dura la Activity (vuelta desde Tratamientos sin loading). */
class IntroViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    var uiState by mutableStateOf<UiState<IntroCatalogData>>(UiState.Loading)
        private set

    init {
        loadCatalog()
    }

    fun loadCatalog(forceRefresh: Boolean = false) {
        val cached = (uiState as? UiState.Success)?.data
        if (!forceRefresh && cached != null) {
            refreshInBackground()
            return
        }

        viewModelScope.launch {
            if (cached == null) {
                uiState = UiState.Loading
            }
            uiState = fetchCatalog()
        }
    }

    private fun refreshInBackground() {
        viewModelScope.launch {
            val fresh = fetchCatalog()
            if (fresh is UiState.Success) {
                uiState = fresh
            }
        }
    }

    private suspend fun fetchCatalog(): UiState<IntroCatalogData> =
        try {
            UiState.Success(
                withContext(Dispatchers.IO) {
                    catalogRepository.getIntroCatalogData()
                },
            )
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error de conexión")
        }
}
