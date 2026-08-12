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
import com.lasante.tvkiosk.ui.components.LaSanteBackground
import com.lasante.tvkiosk.ui.components.LoadingScreen
import com.lasante.tvkiosk.navigation.Args
import com.lasante.tvkiosk.ui.components.UiState
import com.lasante.tvkiosk.data.DisplayTitles

private data class ProductsData(
    val treatmentName: String,
    val treatmentIconUrl: String?,
    val products: List<Product>,
    val isStarProductsMode: Boolean = false,
    val isViewAllTreatments: Boolean = false,
)

private fun starProductsData(
    catalogRepository: CatalogRepository,
    unitId: String,
): ProductsData? {
    val stars = catalogRepository.cachedVitrinaUnitsOrNull()
        ?.firstOrNull { it.unit.id == unitId }
        ?.starProducts
        ?.distinctBy { it.productoId }
        ?: return null
    return ProductsData(
        treatmentName = "Productos Estrella",
        treatmentIconUrl = null,
        products = stars,
        isStarProductsMode = true,
    )
}

@Composable
fun ProductsRoute(
    catalogRepository: CatalogRepository,
    unitId: String,
    treatmentId: String,
    windowSizeClass: WindowSizeClass,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    val isStarProducts = treatmentId == Args.STAR_PRODUCTS_ID
    // Estrellas: /home ya está en memoria desde Intro → Success al instante (sin flash negro).
    var uiState by remember(unitId, treatmentId) {
        mutableStateOf<UiState<ProductsData>>(
            if (isStarProducts) {
                starProductsData(catalogRepository, unitId)
                    ?.let { UiState.Success(it) }
                    ?: UiState.Loading
            } else {
                UiState.Loading
            },
        )
    }
    var retryKey by remember { mutableIntStateOf(0) }
    var selectedProduct by remember(treatmentId) { mutableStateOf<Product?>(null) }

    LaunchedEffect(unitId, treatmentId, retryKey) {
        // No forzar Loading en estrellas si ya hay datos cacheados (evita flash).
        val cachedStars = if (isStarProducts) starProductsData(catalogRepository, unitId) else null
        if (cachedStars == null) {
            uiState = UiState.Loading
        } else if (uiState !is UiState.Success) {
            uiState = UiState.Success(cachedStars)
        }

        uiState = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val isViewAll = treatmentId == Args.ALL_TREATMENTS_ID

                if (isStarProducts) {
                    val stars = catalogRepository.getVitrinaUnits()
                        .firstOrNull { it.unit.id == unitId }
                        ?.starProducts
                        .orEmpty()
                        .distinctBy { it.productoId }
                    UiState.Success(
                        ProductsData(
                            treatmentName = "Productos Estrella",
                            treatmentIconUrl = null,
                            products = stars,
                            isStarProductsMode = true,
                        ),
                    )
                } else {
                    val treatments = catalogRepository.getTreatments(unitId)
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
                            products = products,
                            isViewAllTreatments = isViewAll,
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error de conexión")
        }
    }

    when (val state = uiState) {
        is UiState.Loading -> LaSanteBackground { LoadingScreen() }
        is UiState.Error   -> LaSanteBackground {
            ErrorScreen(message = state.message, onRetry = { retryKey++ })
        }
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
                    isViewAllTreatments = state.data.isViewAllTreatments,
                    isStarProductsMode = state.data.isStarProductsMode,
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
