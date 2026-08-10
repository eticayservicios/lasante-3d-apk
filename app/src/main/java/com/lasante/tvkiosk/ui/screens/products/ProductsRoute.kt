package com.lasante.tvkiosk.ui.screens.products

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lasante.tvkiosk.ui.utils.ModalFrostScrim
import com.lasante.tvkiosk.ui.utils.modalBackdropBlur
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.ui.components.ProductPresentationModal
import com.lasante.tvkiosk.ui.components.ErrorScreen
import com.lasante.tvkiosk.ui.components.LoadingScreen
import com.lasante.tvkiosk.navigation.Args
import com.lasante.tvkiosk.ui.components.UiState
import com.lasante.tvkiosk.data.DisplayTitles

private data class ProductsData(
    val treatmentName: String,
    val treatmentIconUrl: String?,
    val products: List<Product>,
)

@Composable
fun ProductsRoute(
    catalogRepository: CatalogRepository,
    unitId: String,
    treatmentId: String,
    windowSizeClass: WindowSizeClass,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    var uiState by remember { mutableStateOf<UiState<ProductsData>>(UiState.Loading) }
    var retryKey by remember { mutableIntStateOf(0) }
    var selectedProduct by remember(treatmentId) { mutableStateOf<Product?>(null) }

    LaunchedEffect(unitId, treatmentId, retryKey) {
        uiState = UiState.Loading
        uiState = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val treatments = catalogRepository.getTreatments(unitId)
                val isViewAll = treatmentId == Args.ALL_TREATMENTS_ID
                val treatment = treatments.firstOrNull { it.id == treatmentId }
                val products = if (isViewAll) {
                    catalogRepository.getProductsForUnit(unitId)
                } else {
                    catalogRepository.getProducts(treatmentId)
                }
                UiState.Success(
                    ProductsData(
                        treatmentName = when {
                            isViewAll -> "Ver todo"
                            else -> DisplayTitles.resolve(
                                treatment?.name,
                                treatmentId.substringAfter("_").ifBlank { treatmentId },
                            )
                        },
                        treatmentIconUrl = if (isViewAll) null else treatment?.media?.icono,
                        products = products
                    )
                )
            }
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error de conexión")
        }
    }

    when (val state = uiState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error   -> ErrorScreen(message = state.message, onRetry = { retryKey++ })
        is UiState.Success -> Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .modalBackdropBlur(selectedProduct != null),
            ) {
                ProductsScreen(
                    treatmentName     = state.data.treatmentName,
                    treatmentIconUrl  = state.data.treatmentIconUrl,
                    products          = state.data.products,
                    catalogRepository = catalogRepository,
                    unitId            = unitId,
                    isViewAllTreatments = treatmentId == Args.ALL_TREATMENTS_ID,
                    onBack            = onBack,
                    onHome            = onHome,
                    onProductSelected = { product -> selectedProduct = product },
                )
            }

            ModalFrostScrim(visible = selectedProduct != null)

            selectedProduct?.let { product ->
                ProductPresentationModal(
                    product = product,
                    widthClass = windowSizeClass.widthSizeClass,
                    onClose = { selectedProduct = null },
                )
            }
        }
    }
}
