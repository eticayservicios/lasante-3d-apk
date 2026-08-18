package com.lasante.tvkiosk.ui.screens.treatments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.ui.components.ErrorScreen
import com.lasante.tvkiosk.ui.components.LaSanteBackground
import com.lasante.tvkiosk.ui.components.LoadingScreen
import com.lasante.tvkiosk.ui.components.ProductPresentationModal
import com.lasante.tvkiosk.ui.components.UiState
import com.lasante.tvkiosk.ui.utils.ModalFrostScrim
import com.lasante.tvkiosk.ui.utils.modalBackdropBlur

@Composable
fun TreatmentsRoute(
    catalogRepository: CatalogRepository,
    unitId: String,
    windowSizeClass: WindowSizeClass,
    onBack: () -> Unit,
    onTreatmentSelected: (String) -> Unit,
) {
    val activity = LocalContext.current as androidx.activity.ComponentActivity
    val viewModel: TreatmentsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = TreatmentsViewModelFactory(catalogRepository),
    )
    var retryKey by remember { mutableIntStateOf(0) }
    var selectedProduct by remember(unitId) { mutableStateOf<Product?>(null) }

    LaunchedEffect(unitId, retryKey) {
        viewModel.load(unitId, forceRefresh = retryKey > 0)
    }

    when (val state = viewModel.uiState) {
        is UiState.Loading -> LaSanteBackground { LoadingScreen() }
        is UiState.Error -> LaSanteBackground {
            ErrorScreen(message = state.message, onRetry = { retryKey++ })
        }
        is UiState.Success -> Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .modalBackdropBlur(selectedProduct != null),
            ) {
                TreatmentsScreen(
                    unitName = state.data.unitName,
                    treatments = state.data.treatments,
                    products = state.data.products,
                    onBack = onBack,
                    onTreatmentSelected = onTreatmentSelected,
                    onProductSelected = { product -> selectedProduct = product },
                )
            }

            ModalFrostScrim(visible = selectedProduct != null)

            selectedProduct?.let { product ->
                ProductPresentationModal(
                    product = product,
                    businessUnitName = state.data.unitName,
                    widthClass = windowSizeClass.widthSizeClass,
                    onClose = { selectedProduct = null },
                )
            }
        }
    }
}
