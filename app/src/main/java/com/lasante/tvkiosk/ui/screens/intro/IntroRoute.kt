package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.ui.components.ErrorScreen
import com.lasante.tvkiosk.ui.components.LoadingScreen
import com.lasante.tvkiosk.ui.components.UiState

@Composable
fun IntroRoute(
    catalogRepository: CatalogRepository,
    audioPlayer: com.lasante.tvkiosk.media.AudioPlayer,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    contentActive: Boolean = true,
    onNavigateToTreatments: (String) -> Unit = {},
    onNavigateToStarProducts: (String) -> Unit = {},
) {
    val activity = LocalContext.current as androidx.activity.ComponentActivity
    val viewModel: IntroViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = IntroViewModelFactory(catalogRepository),
    )
    val uiState = viewModel.uiState

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is UiState.Loading -> if (contentActive) LoadingScreen()
            is UiState.Error -> if (contentActive) {
                ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.loadCatalog(forceRefresh = true) },
                )
            }
            is UiState.Success -> IntroScreen(
                businessUnits = state.data.businessUnits,
                vitrinaUnits = state.data.vitrinaUnits,
                vitrinaConfig = state.data.vitrinaConfig,
                screenSaverVideos = state.data.screenSaverVideos,
                institutionalVideoUrl = state.data.institutionalVideoUrl,
                allProducts = state.data.allProducts,
                windowSizeClass = windowSizeClass,
                contentActive = contentActive,
                onStopAudio = { audioPlayer.stop() },
                onNavigateToTreatments = onNavigateToTreatments,
                onNavigateToStarProducts = onNavigateToStarProducts,
            )
        }
    }
}
