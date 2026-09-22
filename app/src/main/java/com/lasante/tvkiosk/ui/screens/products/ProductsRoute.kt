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
    val nextCursor: String? = null,
    /** Estrellas de todas las UN (VER TODOS); se pinchan arriba sin esperar paginación. */
    val pinnedStarProducts: List<Product> = emptyList(),
)

/** Estrellas A–Z primero; resto del catálogo A–Z sin duplicar estrellas. */
private fun mergeStarsFirst(stars: List<Product>, catalog: List<Product>): List<Product> {
    val starsSorted = stars.distinctBy { it.productoId }.sortedBy { it.name.lowercase() }
    val starIds = starsSorted.map { it.productoId }.toSet()
    val rest = catalog
        .asSequence()
        .filter { it.productoId !in starIds }
        .distinctBy { it.productoId }
        .sortedBy { it.name.lowercase() }
        .toList()
    return starsSorted + rest
}

private fun starProductsData(
    catalogRepository: CatalogRepository,
    unitId: String,
): ProductsData? {
    val unitKeys = catalogRepository.matchingUnitIds(unitId)
    val stars = catalogRepository.cachedVitrinaUnitsOrNull()
        ?.firstOrNull { it.unit.id in unitKeys }
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
                    val unitKeys = catalogRepository.matchingUnitIds(unitId)
                    val stars = catalogRepository.getVitrinaUnits()
                        .firstOrNull { it.unit.id in unitKeys }
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
                    // Usar snapshot en memoria si está fresco (TTL). No invalidar aquí:
                    // /home tarda ~10s+ (Lambda/Dynamo); invalidar en cada VER TODOS
                    // dejaba Tratamientos/Productos en loading otra vez.
                    val treatments = catalogRepository.getTreatments(unitId)
                    val treatment = treatments.firstOrNull { it.id == treatmentId }
                    val (products, nextCursor, pinnedStars) = if (isViewAll) {
                        // Estrellas de /home (todas las UN) primero; paginación solo completa el resto.
                        val allStars = catalogRepository.getVitrinaUnits()
                            .flatMap { it.starProducts }
                            .distinctBy { it.productoId }
                        val page = catalogRepository.getAllProductsPage(limit = 24)
                        val merged = mergeStarsFirst(allStars, page.items)
                        android.util.Log.i(
                            "ProductsRoute",
                            "VER TODOS stars=${allStars.size} page=${page.items.size} " +
                                "merged=${merged.size} next=${page.nextCursor != null}",
                        )
                        Triple(merged, page.nextCursor, allStars)
                    } else {
                        Triple(catalogRepository.getProducts(treatmentId), null, emptyList())
                    }
                    android.util.Log.i(
                        "ProductsRoute",
                        "unit=$unitId treatment=$treatmentId viewAll=$isViewAll " +
                            "products=${products.size} nextCursor=${nextCursor != null}",
                    )
                    UiState.Success(
                        ProductsData(
                            treatmentName = when {
                                isViewAll -> "Todos los Productos"
                                else -> DisplayTitles.resolve(
                                    treatment?.name,
                                    treatmentId.substringAfter("_").ifBlank { treatmentId },
                                )
                            },
                            treatmentIconUrl = if (isViewAll) null else treatment?.media?.icono,
                            products = products,
                            isViewAllTreatments = isViewAll,
                            nextCursor = nextCursor,
                            pinnedStarProducts = pinnedStars,
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
                    initialNextCursor = state.data.nextCursor,
                    initialStarProducts = state.data.pinnedStarProducts,
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
