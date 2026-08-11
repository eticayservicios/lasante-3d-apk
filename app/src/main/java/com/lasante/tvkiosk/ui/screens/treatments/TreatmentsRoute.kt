package com.lasante.tvkiosk.ui.screens.treatments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.ui.components.ErrorScreen
import com.lasante.tvkiosk.ui.components.LaSanteBackground
import com.lasante.tvkiosk.ui.components.LoadingScreen
import com.lasante.tvkiosk.ui.components.UiState

@Composable
fun TreatmentsRoute(
    catalogRepository: CatalogRepository,
    unitId: String,
    onBack: () -> Unit,
    onTreatmentSelected: (String) -> Unit,
) {
    val activity = LocalContext.current as androidx.activity.ComponentActivity
    val viewModel: TreatmentsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = TreatmentsViewModelFactory(catalogRepository),
    )
    var retryKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(unitId, retryKey) {
        viewModel.load(unitId, forceRefresh = retryKey > 0)
    }

    when (val state = viewModel.uiState) {
        is UiState.Loading -> LaSanteBackground { LoadingScreen() }
        is UiState.Error -> LaSanteBackground {
            ErrorScreen(message = state.message, onRetry = { retryKey++ })
        }
        is UiState.Success -> TreatmentsScreen(
            unitName = state.data.unitName,
            treatments = state.data.treatments,
            onBack = onBack,
            onTreatmentSelected = onTreatmentSelected,
        )
    }
}
