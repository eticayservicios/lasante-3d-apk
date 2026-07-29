package com.lasante.tvkiosk.ui.screens.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lasante.tvkiosk.data.CatalogRepository

class IntroViewModelFactory(
    private val catalogRepository: CatalogRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IntroViewModel::class.java)) {
            return IntroViewModel(catalogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
