package com.lasante.tvkiosk.ui.screens.treatments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lasante.tvkiosk.data.CatalogRepository

class TreatmentsViewModelFactory(
    private val catalogRepository: CatalogRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TreatmentsViewModel::class.java)) {
            return TreatmentsViewModel(catalogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
