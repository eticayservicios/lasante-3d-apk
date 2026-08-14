package com.lasante.tvkiosk.ui.screens.products

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lasante.tvkiosk.BuildConfig
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.data.DisplayTitles
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.ui.components.GreenNavButton
import com.lasante.tvkiosk.ui.components.LaSanteBackground
import com.lasante.tvkiosk.ui.components.LaSanteScreenTitle
import com.lasante.tvkiosk.ui.components.ProductosEstrellasBadge
import com.lasante.tvkiosk.ui.components.RealGreenScrollBar
import com.lasante.tvkiosk.ui.components.TreatmentIconAssets
import com.lasante.tvkiosk.ui.components.TrimTransparentTransformation
import com.lasante.tvkiosk.ui.layout.CatalogHeaderMetrics
import com.lasante.tvkiosk.ui.layout.CatalogScreenTitle
import com.lasante.tvkiosk.ui.layout.DeviceProfile
import com.lasante.tvkiosk.ui.layout.DeviceProfileResolver
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier
import com.lasante.tvkiosk.ui.layout.FireTv42Spacing
import com.lasante.tvkiosk.ui.layout.LogCatalogHeaderProfile
import com.lasante.tvkiosk.ui.layout.layoutForceOverlayLabel
import com.lasante.tvkiosk.ui.layout.rememberCatalogLayout
import com.lasante.tvkiosk.ui.screens.intro.VitrinaUiImages
import com.lasante.tvkiosk.ui.screens.treatments.TreatmentUiMetrics
import com.lasante.tvkiosk.ui.theme.*
import com.lasante.tvkiosk.ui.utils.clickableWithSound
import com.lasante.tvkiosk.ui.utils.UiSound
import com.lasante.tvkiosk.ui.utils.splitProductTitleAndStrength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

private const val CLOSE_NAV_ASSET = "vitrina/ui/close_modal.png"

private val FilterGreenBrush = Brush.horizontalGradient(listOf(FilterGreenStart, FilterGreenEnd))
private val FilterBlueBrush = Brush.horizontalGradient(listOf(FilterBlueStart, FilterBlueEnd))
private val FilterTitleBrush = Brush.horizontalGradient(listOf(FilterGreenStart, FilterGreenEnd))
/** Degradado tipo línea de CT: gris oscuro del título → gris claro (sin verde). */
private val FilterSubtitleBrush = Brush.horizontalGradient(
    listOf(LaSanteText, LaSanteTextSecondary, Color(0xFFD5D8D2)),
)

private enum class SortOrder { NONE, AZ, ZA }

/** Alcance del catálogo en Productos (sustituye Con/Sin imagen). */
private enum class ProductFilter {
    BUSINESS_UNIT,
    THERAPEUTIC_CLASS,
    STAR_PRODUCTS,
}

private fun ProductFilter.label(): String = when (this) {
    ProductFilter.BUSINESS_UNIT -> "Unidad de negocio"
    ProductFilter.THERAPEUTIC_CLASS -> "Clase terapéutica"
    ProductFilter.STAR_PRODUCTS -> "Productos estrellas"
}

/**
 * Catálogo visible según filtro (unidad / CT / estrellas).
 * Con [emptyWhileLoading], mientras carga y la lista de alcance está vacía → lista vacía
 * (evita flash del catálogo completo).
 */
private fun resolveScopeProducts(
    productFilter: ProductFilter,
    products: List<Product>,
    unitProducts: List<Product>,
    starProducts: List<Product>,
    isStarProductsMode: Boolean,
    scopeLoading: Boolean = false,
    emptyWhileLoading: Boolean = false,
): List<Product> = when (productFilter) {
    ProductFilter.BUSINESS_UNIT ->
        if (emptyWhileLoading && scopeLoading && unitProducts.isEmpty()) emptyList()
        else unitProducts.ifEmpty { products }
    ProductFilter.THERAPEUTIC_CLASS ->
        if (isStarProductsMode) {
            if (emptyWhileLoading && scopeLoading && starProducts.isEmpty()) emptyList()
            else starProducts.ifEmpty { products }
        } else {
            products
        }
    ProductFilter.STAR_PRODUCTS ->
        if (emptyWhileLoading && scopeLoading && starProducts.isEmpty()) emptyList()
        else starProducts
}

private sealed class ProductGridVisual {
    data class Photo(val url: String) : ProductGridVisual()
    data object Placeholder : ProductGridVisual()
}

/**
 * Grilla: solo imágenes 2D (SceneView en LazyGrid rompe clics y se monta sobre el header).
 * GLB solo en el modal al abrir el producto.
 * Orden alineado con burbujas / modal: miniatura → principal → vista previa 3D.
 */
private fun Product.gridVisual(): ProductGridVisual {
    val miniatura = media.imagenes2d.miniatura?.trim()?.takeIf { it.isNotBlank() }
    val principal = media.imagenes2d.principal?.trim()?.takeIf { it.isNotBlank() }
    val preview = media.modelo3d.vistaPrevia?.trim()?.takeIf { it.isNotBlank() }
    return when {
        miniatura != null -> ProductGridVisual.Photo(miniatura)
        principal != null -> ProductGridVisual.Photo(principal)
        preview != null -> ProductGridVisual.Photo(preview)
        else -> ProductGridVisual.Placeholder
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    treatmentName: String,
    treatmentIconUrl: String?,
    products: List<Product>,
    catalogRepository: CatalogRepository,
    unitId: String,
    isViewAllTreatments: Boolean = false,
    isStarProductsMode: Boolean = false,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onProductSelected: (Product) -> Unit,
) {
    BackHandler(onBack = onBack)

    LaSanteBackground {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = maxWidth
            val canvasHeight = maxHeight
            val catalog = rememberCatalogLayout(canvasWidth, canvasHeight)
            val profile = catalog.profile
            val nav = catalog.nav
            val header = catalog.header
            val isLandscape = catalog.isLandscape
            val isTv66 = catalog.isTv66
            val isTv42 = catalog.isTv42
            val isTv42LargeUp = catalog.largeCanvas
            val isPhone = catalog.isPhone
            val columns = when {
                profile.tier == DeviceProfileTier.COMPACT_PORTRAIT -> 2
                else -> if (isLandscape) 4 else 2
            }
            val buttonSize = header.navButtonSize
            LogCatalogHeaderProfile(
                header = header,
                screen = if (isStarProductsMode) "ProductosEstrella" else "Products",
            )

            var searchQuery by remember { mutableStateOf("") }
            var sortOrder by remember { mutableStateOf(SortOrder.NONE) }
            val defaultFilter = when {
                isStarProductsMode -> ProductFilter.STAR_PRODUCTS
                isViewAllTreatments -> ProductFilter.BUSINESS_UNIT
                else -> ProductFilter.THERAPEUTIC_CLASS
            }
            var productFilter by remember(unitId, isViewAllTreatments, isStarProductsMode) {
                mutableStateOf(defaultFilter)
            }
            var showFilterSheet by remember { mutableStateOf(false) }
            /** Filtro CT (presentación + estrellas, AND). UI del sheet nuevo va al final. */
            var ctCatalogFilter by remember(unitId) { mutableStateOf(TherapeuticClassCatalogFilter()) }
            var isSearching by remember { mutableStateOf(false) }
            var globalSearchResults by remember { mutableStateOf<List<Product>>(emptyList()) }
            var unitProducts by remember(unitId) { mutableStateOf<List<Product>>(emptyList()) }
            // Modo estrellas: la ruta ya trae los slots de /home; evita lista vacía al abrir.
            var starProducts by remember(unitId, isStarProductsMode, products) {
                mutableStateOf(if (isStarProductsMode) products else emptyList())
            }
            var scopeLoading by remember(unitId) { mutableStateOf(!isStarProductsMode) }

            val coroutineScope = rememberCoroutineScope()

            // Carga en IO: en Main bloqueaba el UI (ANR al filtrar Unidad de negocio).
            // getVitrinaUnits / getProductsForUnit usan snapshot /home o catálogo ya cacheado.
            LaunchedEffect(unitId, isStarProductsMode) {
                scopeLoading = true
                val loaded = withContext(Dispatchers.IO) {
                    runCatching {
                        val stars = catalogRepository.getVitrinaUnits()
                            .firstOrNull { it.unit.id == unitId }
                            ?.starProducts
                            .orEmpty()
                            .distinctBy { it.productoId }
                        // En modo estrellas no hace falta el catálogo completo de la unidad al abrir.
                        val unit = if (isStarProductsMode) {
                            emptyList()
                        } else {
                            catalogRepository.getProductsForUnit(unitId)
                        }
                        unit to stars
                    }.getOrElse { emptyList<Product>() to emptyList() }
                }
                unitProducts = loaded.first
                if (loaded.second.isNotEmpty()) {
                    starProducts = loaded.second
                } else if (isStarProductsMode) {
                    starProducts = products
                }
                scopeLoading = false
            }

            // Si en modo estrellas el usuario cambia a "Unidad de negocio", cargar catálogo bajo demanda.
            LaunchedEffect(productFilter, unitId) {
                if (productFilter != ProductFilter.BUSINESS_UNIT) return@LaunchedEffect
                if (unitProducts.isNotEmpty()) return@LaunchedEffect
                scopeLoading = true
                unitProducts = withContext(Dispatchers.IO) {
                    runCatching { catalogRepository.getProductsForUnit(unitId) }
                        .getOrElse { emptyList() }
                }
                scopeLoading = false
            }

            val starProductIds = remember(starProducts) {
                starProducts.map { it.productoId }.toSet()
            }

            LaunchedEffect(searchQuery, products, unitProducts, starProducts, productFilter, ctCatalogFilter, starProductIds) {
                if (searchQuery.length < 3) {
                    globalSearchResults = emptyList()
                    isSearching = false
                    return@LaunchedEffect
                }
                isSearching = true
                kotlinx.coroutines.delay(500)
                val query = searchQuery
                val scopeProducts = resolveScopeProducts(
                    productFilter = productFilter,
                    products = products,
                    unitProducts = unitProducts,
                    starProducts = starProducts,
                    isStarProductsMode = isStarProductsMode,
                )
                val searchable = if (
                    !isStarProductsMode &&
                    !isViewAllTreatments &&
                    productFilter == ProductFilter.THERAPEUTIC_CLASS
                ) {
                    applyTherapeuticClassCatalogFilter(scopeProducts, starProductIds, ctCatalogFilter)
                } else {
                    scopeProducts
                }
                try {
                    val localResults = withContext(Dispatchers.Default) {
                        searchable.filter {
                            it.nombre.contains(query, ignoreCase = true) ||
                                it.descripcion.contains(query, ignoreCase = true)
                        }
                    }
                    globalSearchResults = if (localResults.isNotEmpty()) {
                        localResults
                    } else {
                        val result = catalogRepository.search(query, "productos")
                        result.items.map { item ->
                            Product(
                                productoId = item.id,
                                unidadId = "",
                                tratamientoId = "",
                                nombre = item.nombre,
                                descripcion = item.descripcion ?: "",
                                estado = "ACTIVO",
                                orden = 0,
                                media = com.lasante.tvkiosk.data.ProductMedia(
                                    imagenes2d = com.lasante.tvkiosk.data.Images2D(null, null, emptyList()),
                                    modelo3d = com.lasante.tvkiosk.data.Model3D(null, null, null),
                                ),
                                atributos = emptyMap(),
                                dosisValor = item.dosisValor,
                                dosisUnidad = item.dosisUnidad,
                            )
                        }
                    }
                } catch (_: Exception) {
                    globalSearchResults = withContext(Dispatchers.Default) {
                        searchable.filter {
                            it.nombre.contains(query, ignoreCase = true) ||
                                it.descripcion.contains(query, ignoreCase = true)
                        }
                    }
                }
                isSearching = false
            }

            val filteredProducts = remember(
                products,
                unitProducts,
                starProducts,
                searchQuery,
                globalSearchResults,
                productFilter,
                sortOrder,
                scopeLoading,
                isStarProductsMode,
                ctCatalogFilter,
                starProductIds,
            ) {
                val scopeProducts = resolveScopeProducts(
                    productFilter = productFilter,
                    products = products,
                    unitProducts = unitProducts,
                    starProducts = starProducts,
                    isStarProductsMode = isStarProductsMode,
                    scopeLoading = scopeLoading,
                    emptyWhileLoading = true,
                )
                val ctScopedProducts = if (
                    !isStarProductsMode &&
                    !isViewAllTreatments &&
                    productFilter == ProductFilter.THERAPEUTIC_CLASS
                ) {
                    applyTherapeuticClassCatalogFilter(scopeProducts, starProductIds, ctCatalogFilter)
                } else {
                    scopeProducts
                }
                val displayProducts = if (searchQuery.length >= 3) {
                    globalSearchResults
                } else {
                    ctScopedProducts.filter {
                        searchQuery.isBlank() ||
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.description.contains(searchQuery, ignoreCase = true)
                    }
                }
                when (sortOrder) {
                    SortOrder.AZ -> displayProducts.sortedBy { it.name }
                    SortOrder.ZA -> displayProducts.sortedByDescending { it.name }
                    SortOrder.NONE -> displayProducts
                }
            }

            val gridState = rememberLazyGridState()

            val scrollInfo = remember(columns) {
                derivedStateOf { computeProductsGridScrollbar(gridState, columns) }
            }

            val showScrollbar = filteredProducts.isNotEmpty()
            val horizontalPadding = catalog.horizontalPadding
            val contentPadding = catalog.contentPadding
            val gridMaxWidth = catalog.gridMaxWidth

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = catalog.topPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = gridMaxWidth)
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                    ) {
                        if (isStarProductsMode) {
                            // Mock: badge | search | Home + Close; Ordenar bajo el buscador (sin filtro ni título).
                            StarProductsHeader(
                                header = header,
                                contentPadding = contentPadding,
                                buttonSize = buttonSize,
                                isLandscape = isLandscape,
                                isTv66 = isTv66,
                                isTv42 = isTv42,
                                isTv42LargeUp = isTv42LargeUp,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                isSearching = isSearching,
                                sortOrder = sortOrder,
                                onSortClick = {
                                    sortOrder = when (sortOrder) {
                                        SortOrder.NONE -> SortOrder.AZ
                                        SortOrder.AZ -> SortOrder.ZA
                                        SortOrder.ZA -> SortOrder.NONE
                                    }
                                },
                                onHome = onHome,
                                onClose = onBack,
                            )
                        } else if (header.usesSharedTvCatalogLayout) {
                            // Familia Fire/Ariana/TV66: título | filtro | search | nav; Ordenar aparte.
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = contentPadding)
                                        .padding(top = header.controlsTopGap),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    CatalogScreenTitle(
                                        text = DisplayTitles.resolve(treatmentName),
                                        nav = nav,
                                        titleStartGap = header.titleStartGap,
                                        titleTopGap = 0.dp,
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    val filterContext = LocalContext.current
                                    val filterSize = header.filterIconSize
                                    val filterModel = remember(filterContext) {
                                        VitrinaUiImages.filterRequest(filterContext)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(filterSize)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = {
                                                    coroutineScope.launch {
                                                        yield()
                                                        delay(32)
                                                        showFilterSheet = true
                                                    }
                                                },
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        AsyncImage(
                                            model = filterModel,
                                            contentDescription = "Filtrar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit,
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(header.filterToSearchGap))
                                    Box(
                                        modifier = Modifier
                                            .width(header.searchBarWidth)
                                            .height(header.searchBarHeight)
                                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(50.dp))
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFFF8F8F8), Color(0xFFD0D0D0)),
                                                ),
                                            )
                                            .padding(horizontal = if (isLandscape) 10.dp else 8.dp),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            AsyncImage(
                                                model = "file:///android_asset/vitrina/ui/search_icon.png",
                                                contentDescription = null,
                                                modifier = Modifier.size(header.searchIconSize),
                                                contentScale = ContentScale.Fit,
                                            )
                                            BasicTextField(
                                                value = searchQuery,
                                                onValueChange = { searchQuery = it },
                                                textStyle = TextStyle(
                                                    color = LaSanteText,
                                                    fontSize = header.searchFontSize,
                                                ),
                                                cursorBrush = SolidColor(LaSanteGreen),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                decorationBox = { innerTextField ->
                                                    if (searchQuery.isEmpty()) {
                                                        Text(
                                                            "Buscar Producto",
                                                            color = LaSanteTextSecondary.copy(alpha = 0.40f),
                                                            fontSize = header.searchFontSize,
                                                        )
                                                    }
                                                    innerTextField()
                                                },
                                            )
                                            if (isSearching) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = LaSanteGreen,
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(header.searchToNavGap))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(header.navPairSpacing),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        GreenNavButton(
                                            assetPath = "svg/ui/Before.svg",
                                            contentDescription = "Volver",
                                            onClick = onBack,
                                            size = buttonSize,
                                        )
                                        GreenNavButton(
                                            assetPath = "svg/ui/Home.svg",
                                            contentDescription = "Inicio",
                                            onClick = onHome,
                                            size = buttonSize,
                                            playSound = true,
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = contentPadding),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    // Ordenar bajo el buscador: end = nav + gaps + search width.
                                    ProductsSortButton(
                                        sortOrder = sortOrder,
                                        onSortClick = {
                                            sortOrder = when (sortOrder) {
                                                SortOrder.NONE -> SortOrder.AZ
                                                SortOrder.AZ -> SortOrder.ZA
                                                SortOrder.ZA -> SortOrder.NONE
                                            }
                                        },
                                        isLandscape = isLandscape,
                                        isTv66 = isTv66,
                                        isTv42 = isTv42,
                                        isTv42LargeUp = isTv42LargeUp,
                                        sortScale = header.sortScale,
                                        modifier = Modifier.padding(
                                            top = header.sortTopGap,
                                            end = buttonSize * 2f + header.navPairSpacing + header.searchToNavGap,
                                        ),
                                    )
                                }
                            }
                        } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = contentPadding),
                            verticalAlignment = Alignment.Top,
                        ) {
                            CatalogScreenTitle(
                                text = DisplayTitles.resolve(treatmentName),
                                nav = nav,
                                titleStartGap = header.titleStartGap,
                                titleTopGap = header.titleTopGap,
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Row(verticalAlignment = Alignment.Top) {
                                val filterContext = LocalContext.current
                                val filterSize = header.filterIconSize
                                val filterModel = remember(filterContext) {
                                    VitrinaUiImages.filterRequest(filterContext)
                                }
                                val filterTopPad =
                                    header.centerOnSearchBar(filterSize) + header.controlsTopGap
                                Box(
                                    modifier = Modifier
                                        .padding(top = filterTopPad)
                                        .offset(x = header.filterOffsetX)
                                        .size(filterSize)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                coroutineScope.launch {
                                                    yield()
                                                    delay(32)
                                                    showFilterSheet = true
                                                }
                                            },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    AsyncImage(
                                        model = filterModel,
                                        contentDescription = "Filtrar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit,
                                    )
                                }

                                Spacer(modifier = Modifier.width(header.filterToSearchGap))

                                Column(
                                    modifier = Modifier.widthIn(max = header.searchBarWidth),
                                    horizontalAlignment = Alignment.End,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = header.controlsTopGap)
                                            .width(header.searchBarWidth)
                                            .height(header.searchBarHeight)
                                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(50.dp))
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFFF8F8F8), Color(0xFFD0D0D0)),
                                                ),
                                            )
                                            .padding(horizontal = if (isLandscape) 10.dp else 8.dp),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            AsyncImage(
                                                model = "file:///android_asset/vitrina/ui/search_icon.png",
                                                contentDescription = null,
                                                modifier = Modifier.size(header.searchIconSize),
                                                contentScale = ContentScale.Fit,
                                            )
                                            BasicTextField(
                                                value = searchQuery,
                                                onValueChange = { searchQuery = it },
                                                textStyle = TextStyle(
                                                    color = LaSanteText,
                                                    fontSize = header.searchFontSize,
                                                ),
                                                cursorBrush = SolidColor(LaSanteGreen),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                decorationBox = { innerTextField ->
                                                    if (searchQuery.isEmpty()) {
                                                        Text(
                                                            "Buscar Producto",
                                                            color = LaSanteTextSecondary.copy(alpha = 0.40f),
                                                            fontSize = header.searchFontSize,
                                                        )
                                                    }
                                                    innerTextField()
                                                },
                                            )
                                            if (isSearching) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(
                                                        if (isTv42LargeUp) 16.dp else 12.dp,
                                                    ),
                                                    strokeWidth = 2.dp,
                                                    color = LaSanteGreen,
                                                )
                                            }
                                        }
                                    }

                                    ProductsSortButton(
                                        sortOrder = sortOrder,
                                        onSortClick = {
                                            sortOrder = when (sortOrder) {
                                                SortOrder.NONE -> SortOrder.AZ
                                                SortOrder.AZ -> SortOrder.ZA
                                                SortOrder.ZA -> SortOrder.NONE
                                            }
                                        },
                                        isLandscape = isLandscape,
                                        isTv66 = isTv66,
                                        isTv42 = isTv42,
                                        isTv42LargeUp = isTv42LargeUp,
                                        sortScale = header.sortScale,
                                        modifier = Modifier.padding(top = header.sortTopGap),
                                    )
                                }

                                Spacer(modifier = Modifier.width(header.searchToNavGap))

                                val navTopPad = header.navButtonsTopGap(buttonSize)
                                val navSpacing = nav.buttonSpacing
                                Row(
                                    modifier = Modifier.padding(top = navTopPad),
                                    horizontalArrangement = Arrangement.spacedBy(navSpacing),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    GreenNavButton(
                                        assetPath = "svg/ui/Before.svg",
                                        contentDescription = "Volver",
                                        onClick = onBack,
                                        size = buttonSize,
                                    )
                                    GreenNavButton(
                                        assetPath = "svg/ui/Home.svg",
                                        contentDescription = "Inicio",
                                        onClick = onHome,
                                        size = buttonSize,
                                        playSound = true,
                                    )
                                }
                            }
                        }
                        }

                        val scrollRailWidth = when {
                            isTv42LargeUp -> 48.dp
                            isLandscape -> 36.dp
                            else -> 32.dp
                        }
                        val scrollArrowSize = when {
                            isTv42LargeUp -> 36.dp
                            else -> 24.dp
                        }
                        val scrollEndPad = CatalogHeaderMetrics.scrollEndUnderHome(contentPadding)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            val waitingScope = scopeLoading && (
                                productFilter == ProductFilter.BUSINESS_UNIT ||
                                    productFilter == ProductFilter.STAR_PRODUCTS
                                )
                            if (waitingScope) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = LaSanteGreen,
                                )
                            } else {
                            LazyVerticalGrid(
                                state = gridState,
                                columns = GridCells.Fixed(columns),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = contentPadding,
                                    end = if (showScrollbar) {
                                        scrollEndPad + scrollRailWidth + 8.dp
                                    } else {
                                        contentPadding
                                    },
                                    top = CatalogHeaderMetrics.productsGridTopPadding(),
                                    bottom = 32.dp,
                                ),
                                verticalArrangement = Arrangement.spacedBy(
                                    when {
                                        isTv66 -> catalog.grid.cardSpacing
                                        isTv42LargeUp -> 22.dp
                                        else -> 16.dp
                                    },
                                ),
                                horizontalArrangement = Arrangement.spacedBy(
                                    when {
                                        isTv66 -> catalog.grid.cardSpacing
                                        isTv42LargeUp -> 22.dp
                                        else -> 16.dp
                                    },
                                ),
                            ) {
                                items(
                                    count = filteredProducts.size,
                                    key = { index -> filteredProducts[index].productoId },
                                ) { index ->
                                    ProductGridItem(
                                        product = filteredProducts[index],
                                        isLandscape = isLandscape,
                                        isPhone = isPhone,
                                        isTv42 = isTv42,
                                        isTv66 = isTv66,
                                        isTv42LargeUp = isTv42LargeUp,
                                        blockWidthFraction = header.productBlockWidthFraction,
                                        onClick = { onProductSelected(filteredProducts[index]) },
                                    )
                                }
                            }

                            if (showScrollbar) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = scrollEndPad, top = 8.dp, bottom = 24.dp)
                                        .width(scrollRailWidth)
                                        .fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch { gridState.animateScrollToItem(0) }
                                        },
                                        modifier = Modifier.size(scrollArrowSize + 8.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.KeyboardArrowUp,
                                            contentDescription = null,
                                            tint = LaSanteGreen,
                                            modifier = Modifier.size(scrollArrowSize),
                                        )
                                    }
                                    RealGreenScrollBar(
                                        scrollFraction = scrollInfo.value.scrollFraction,
                                        thumbFraction = scrollInfo.value.thumbFraction,
                                        modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                    )
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                gridState.animateScrollToItem(
                                                    (filteredProducts.size - 1).coerceAtLeast(0),
                                                )
                                            }
                                        },
                                        modifier = Modifier.size(scrollArrowSize + 8.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = LaSanteGreen,
                                            modifier = Modifier.size(scrollArrowSize),
                                        )
                                    }
                                }
                            }
                            } // waitingScope else
                        }
                    }
                }

                // Badge superior izquierdo: CT en productos; Productos Estrellas en modo estrellas.
                val topBadgeModifier = Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        val (dx, lift) = when {
                            isTv66 -> 0.dp to FireTv42Spacing.spaces(7)
                            isTv42 || catalog.largeCanvas ->
                                (-FireTv42Spacing.spaces(9)) to FireTv42Spacing.spaces(4)
                            else -> 0.dp to 0.dp
                        }
                        // Estrellas: bajar 5 espacios respecto al ancla CT.
                        val y = if (isStarProductsMode) {
                            -lift + FireTv42Spacing.spaces(5)
                        } else {
                            -lift
                        }
                        IntOffset(x = dx.roundToPx(), y = y.roundToPx())
                    }
                    .padding(
                        start = horizontalPadding + contentPadding,
                        top = 0.dp,
                    )
                if (isStarProductsMode) {
                    ProductosEstrellasBadge(
                        height = catalog.ui.badgeHeight.coerceAtMost(header.searchBarHeight * 1.35f),
                        modifier = topBadgeModifier,
                    )
                } else {
                    TreatmentIconBadge(
                        iconUrl = treatmentIconUrl,
                        metrics = catalog.ui,
                        modifier = topBadgeModifier,
                    )
                }

                // DEBUG arriba: abajo tapaba la 2.ª fila (parecía que los cards “se montaban”).
                if (BuildConfig.DEBUG) {
                    Text(
                        text = "${canvasWidth.value.toInt()}×${canvasHeight.value.toInt()} · ${profile.tier} · " +
                            "large=${header.isLargeCanvas} · btn=${buttonSize.value}dp · " +
                            "filter=${header.filterIconSize.value}dp · " +
                            layoutForceOverlayLabel() +
                            if (header.isTv66 || profile.tier.name.contains("LARGE")) {
                                " · TV66-ref=1280×720"
                            } else if (header.isTv42) {
                                " · Ariana-ref=1137×711"
                            } else {
                                ""
                            },
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                            .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            if (showFilterSheet) {
                val useCtCatalogSheet =
                    !isStarProductsMode &&
                        !isViewAllTreatments &&
                        productFilter == ProductFilter.THERAPEUTIC_CLASS
                if (useCtCatalogSheet) {
                    TherapeuticClassFilterSheet(
                        initialFilter = ctCatalogFilter,
                        profile = profile,
                        onApply = { applied ->
                            showFilterSheet = false
                            coroutineScope.launch {
                                yield()
                                delay(48)
                                ctCatalogFilter = applied
                            }
                        },
                        onClear = {
                            // Limpia sin cerrar el modal; el draft se resetea en el sheet.
                            coroutineScope.launch {
                                yield()
                                delay(48)
                                ctCatalogFilter = TherapeuticClassCatalogFilter()
                            }
                        },
                        onDismiss = { showFilterSheet = false },
                    )
                } else {
                    FilterBottomSheet(
                        selectedFilter = productFilter,
                        onFilterSelected = { selected ->
                            showFilterSheet = false
                            coroutineScope.launch {
                                yield()
                                delay(48)
                                productFilter = selected
                            }
                        },
                        onClearFilters = {
                            showFilterSheet = false
                            coroutineScope.launch {
                                yield()
                                delay(48)
                                productFilter = defaultFilter
                                ctCatalogFilter = TherapeuticClassCatalogFilter()
                                searchQuery = ""
                                sortOrder = SortOrder.NONE
                            }
                        },
                        onDismiss = { showFilterSheet = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun StarProductsHeader(
    header: CatalogHeaderMetrics,
    contentPadding: Dp,
    buttonSize: Dp,
    isLandscape: Boolean,
    isTv66: Boolean,
    isTv42: Boolean,
    isTv42LargeUp: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearching: Boolean,
    sortOrder: SortOrder,
    onSortClick: () -> Unit,
    onHome: () -> Unit,
    onClose: () -> Unit,
) {
    val navSpacing = if (header.navPairSpacing > 0.dp) {
        header.navPairSpacing
    } else {
        header.navButtonSize * 0.12f
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = contentPadding)
                .padding(top = header.controlsTopGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(header.searchBarWidth)
                    .height(header.searchBarHeight)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(50.dp))
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFF8F8F8), Color(0xFFD0D0D0)),
                        ),
                    )
                    .padding(horizontal = if (isLandscape) 10.dp else 8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    AsyncImage(
                        model = "file:///android_asset/vitrina/ui/search_icon.png",
                        contentDescription = null,
                        modifier = Modifier.size(header.searchIconSize),
                        contentScale = ContentScale.Fit,
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        textStyle = TextStyle(
                            color = LaSanteText,
                            fontSize = header.searchFontSize,
                        ),
                        cursorBrush = SolidColor(LaSanteGreen),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Buscar Producto",
                                    color = LaSanteTextSecondary.copy(alpha = 0.40f),
                                    fontSize = header.searchFontSize,
                                )
                            }
                            innerTextField()
                        },
                    )
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = LaSanteGreen,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(header.searchToNavGap))
            Row(
                horizontalArrangement = Arrangement.spacedBy(navSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GreenNavButton(
                    assetPath = "svg/ui/Home.svg",
                    contentDescription = "Inicio",
                    onClick = onHome,
                    size = buttonSize,
                    playSound = true,
                )
                GreenNavButton(
                    assetPath = CLOSE_NAV_ASSET,
                    contentDescription = "Cerrar",
                    onClick = onClose,
                    size = buttonSize,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = contentPadding),
            horizontalArrangement = Arrangement.End,
        ) {
            ProductsSortButton(
                sortOrder = sortOrder,
                onSortClick = onSortClick,
                isLandscape = isLandscape,
                isTv66 = isTv66,
                isTv42 = isTv42,
                isTv42LargeUp = isTv42LargeUp,
                sortScale = header.sortScale,
                modifier = Modifier.padding(
                    top = header.sortTopGap,
                    end = buttonSize * 2f + navSpacing + header.searchToNavGap,
                ),
            )
        }
    }
}

@Composable
private fun ProductsSortButton(
    sortOrder: SortOrder,
    onSortClick: () -> Unit,
    isLandscape: Boolean,
    isTv66: Boolean,
    isTv42: Boolean = false,
    isTv42LargeUp: Boolean = false,
    sortScale: Float = 1f,
    modifier: Modifier = Modifier,
) {
    // TV66 usa la base largeCanvas y aplica sortScale (+10%).
    val horizontalPad = when {
        isTv66 || isTv42LargeUp -> 28.dp
        isLandscape -> 20.dp
        else -> 16.dp
    } * sortScale
    val verticalPad = when {
        isTv66 || isTv42LargeUp -> 3.dp
        isTv42 -> 1.dp
        isLandscape -> 1.dp
        else -> 2.dp
    } * sortScale
    val labelSize = when {
        isTv66 || isTv42LargeUp -> 14.sp
        isLandscape -> 11.sp
        else -> 10.sp
    } * sortScale
    val iconSize = when {
        isTv66 || isTv42LargeUp -> 20.dp
        isLandscape -> 14.dp
        else -> 12.dp
    } * sortScale

    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Color.Transparent,
        onClick = onSortClick,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Brush.horizontalGradient(listOf(LaSanteGreenDark, LaSanteGreen)))
                .padding(horizontal = horizontalPad, vertical = verticalPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp * sortScale),
        ) {
            Text(
                text = when (sortOrder) {
                    SortOrder.NONE -> "Ordenar A - Z"
                    SortOrder.AZ -> "A - Z"
                    SortOrder.ZA -> "Z - A"
                },
                color = Color.White,
                fontSize = labelSize,
                fontWeight = FontWeight.Bold,
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@Composable
private fun TreatmentIconBadge(
    iconUrl: String?,
    metrics: TreatmentUiMetrics.ProfileMetrics,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val iconModel = TreatmentIconAssets.resolve(iconUrl = iconUrl)
    val badgeHeight = metrics.badgeHeight
    val badgeWidth =
        (badgeHeight * TreatmentUiMetrics.BADGE_WIDTH_TO_HEIGHT).coerceAtLeast(24.dp)
    val iconSize = metrics.badgeIconSize
    // Badge PNG es pequeño; el icono CT del CDN es ~1080² — no usar ORIGINAL (OOM + Trim).
    val badgeDecodePx = with(density) {
        maxOf(badgeWidth, badgeHeight).times(2f).roundToPx().coerceIn(64, 256)
    }
    val badgeBgModel = remember(context, badgeDecodePx) {
        ImageRequest.Builder(context)
            .data("file:///android_asset/vitrina/ui/treatment_badge_shadow.png")
            .size(badgeDecodePx)
            .crossfade(false)
            .build()
    }
    val iconDecodePx = with(density) {
        (iconSize * 3f).roundToPx().coerceIn(96, 256)
    }

    Box(
        modifier = modifier
            .width(badgeWidth)
            .height(badgeHeight),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = badgeBgModel,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        if (!iconModel.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(iconModel)
                    .size(iconDecodePx)
                    .transformations(TrimTransparentTransformation())
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                // −2.dp respecto al centro del fondo.
                modifier = Modifier
                    .size(iconSize)
                    .offset(y = 2.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun ProductGridItem(
    product: Product,
    isLandscape: Boolean,
    isPhone: Boolean = false,
    isTv42: Boolean = false,
    isTv66: Boolean = false,
    isTv42LargeUp: Boolean = false,
    blockWidthFraction: Float = 1f,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    // Imagen dentro del cuadrado gris (−10%).
    val imageFillFraction = when {
        isPhone -> 0.5436f
        isTv42LargeUp -> 0.738f
        isTv42 -> 0.702f
        isTv66 -> 0.792f
        else -> 0.765f
    }
    val titleFontSize = when {
        isTv42LargeUp -> 17.sp
        isTv66 -> 16.sp
        isLandscape -> 13.sp
        else -> 11.sp
    }
    val strengthFontSize = when {
        isTv42LargeUp -> 15.sp
        isTv66 -> 14.sp
        isLandscape -> 12.sp
        else -> 10.sp
    }
    val descriptionFontSize = when {
        isTv42LargeUp -> 13.sp
        isTv66 -> 12.sp
        isLandscape -> 11.sp
        else -> 9.sp
    }
    val titleBrush = Brush.horizontalGradient(
        listOf(LaSanteGreenDark, LaSanteGreen, Color(0xFFA8C829)),
    )
    val (titlePart, strengthPart) = remember(product.name, product.dosisDisplay) {
        val apiDosis = product.dosisDisplay
        if (!apiDosis.isNullOrBlank()) {
            product.name.trim() to apiDosis
        } else {
            splitProductTitleAndStrength(product.name)
        }
    }
    val shortDescription = remember(product.description, product.name) {
        productCardShortDescription(product.description, product.name)
    }
    val innerPad = when {
        isTv42LargeUp || isTv66 -> 10.dp
        else -> 6.dp
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth(blockWidthFraction)
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        val gridVisual = product.gridVisual()
        // Cuadrado: side = ancho de celda; el clic solo en el cuadro (no el texto).
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val side = maxWidth
            val gridImageSizePx = with(density) { (side * imageFillFraction).roundToPx() }
            Box(
                modifier = Modifier
                    .size(side)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFE7E7E7), Color(0xFFF7F7F7)),
                        ),
                    )
                    .clickableWithSound(sound = UiSound.Product) { onClick() }
                    .padding(innerPad),
                contentAlignment = Alignment.Center,
            ) {
                when (gridVisual) {
                    is ProductGridVisual.Photo -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(gridVisual.url)
                                .size(gridImageSizePx.coerceAtLeast(1))
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize(imageFillFraction)
                                .align(Alignment.Center),
                            contentScale = ContentScale.Fit,
                        )
                    }
                    ProductGridVisual.Placeholder -> Text(
                        "📦",
                        fontSize = when {
                            isPhone -> if (isLandscape) 22.sp else 18.sp
                            isTv66 -> 56.sp
                            isLandscape -> 42.sp
                            else -> 36.sp
                        },
                        color = Color.Gray,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(
                    horizontal = if (isTv66) 10.dp else 8.dp,
                    vertical = if (isTv66) 6.dp else 4.dp,
                )
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = titlePart,
                style = TextStyle(
                    brush = titleBrush,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.Unspecified,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!strengthPart.isNullOrBlank()) {
                Text(
                    text = strengthPart,
                    color = LaSanteTextSecondary,
                    fontSize = strengthFontSize,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!shortDescription.isNullOrBlank()) {
                Text(
                    text = shortDescription,
                    color = LaSanteTextSecondary.copy(alpha = 0.85f),
                    fontSize = descriptionFontSize,
                    fontWeight = FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    }
}

/**
 * Descripción corta para el card de Productos (campo [Product.descripcion]).
 * Omite vacíos o texto idéntico al nombre.
 */
private fun productCardShortDescription(rawDescription: String, productName: String): String? {
    val text = rawDescription.trim()
    if (text.isEmpty()) return null
    if (text.equals(productName.trim(), ignoreCase = true)) return null
    return text
}

@Composable
private fun TherapeuticClassFilterSheet(
    initialFilter: TherapeuticClassCatalogFilter,
    profile: DeviceProfile,
    onApply: (TherapeuticClassCatalogFilter) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    var draftFormas by remember(initialFilter) { mutableStateOf(initialFilter.formas) }
    var draftStarsOnly by remember(initialFilter) { mutableStateOf(initialFilter.starProductsOnly) }
    var presentationExpanded by remember { mutableStateOf(false) }
    val poppins = MaterialTheme.typography.bodyLarge.fontFamily
    val m = remember(profile) { DeviceProfileResolver.filterSheetMetrics(profile) }
    val filterActionShape = RoundedCornerShape(50.dp)
    val filterSelectShape = RoundedCornerShape(50.dp)
    val filterSubtitleStyle = TextStyle(
        brush = FilterSubtitleBrush,
        fontFamily = poppins,
        fontWeight = FontWeight.Normal,
    )
    val actionModifier = if (m.actionFillColumn) {
        Modifier
            .fillMaxWidth(0.9f)
            .height(m.actionHeight)
    } else {
        Modifier
            .width(m.actionWidth)
            .height(m.actionHeight)
    }

    BackHandler(onBack = onDismiss)

    // Scrim y contenido separados (evita clickable anidado → ANR/input timeout).
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(m.widthFraction)
                .widthIn(max = m.maxWidth)
                .fillMaxHeight(m.heightFraction)
                .shadow(8.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(horizontal = m.paddingH)
                .padding(top = m.paddingTop, bottom = m.paddingBottom),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF2F2F2F)),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LaSanteScreenTitle(
                    text = "Filtros",
                    fontSize = m.titleSp,
                    textBrush = FilterTitleBrush,
                    underlineBrush = Brush.horizontalGradient(
                        listOf(Color(0xFF8FA88A), Color(0xFFD5D8D2), Color.White),
                    ),
                    underlineMatchTextWidth = true,
                    underlineWidth = m.titleUnderlineWidth,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    allCaps = false,
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.weight(1f))
                GreenNavButton(
                    assetPath = CLOSE_NAV_ASSET,
                    contentDescription = "Cerrar",
                    size = m.closeSize,
                    onClick = onDismiss,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(m.columnGap),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Presentación del producto",
                        modifier = Modifier.fillMaxWidth(),
                        style = filterSubtitleStyle.copy(fontSize = m.subtitleSp.sp),
                        textAlign = TextAlign.Start,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(m.selectHeight)
                            .clip(filterSelectShape)
                            .background(FilterGreenBrush)
                            .clickable { presentationExpanded = !presentationExpanded }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = when {
                                draftFormas.isEmpty() -> "Seleccionar presentación"
                                draftFormas.size == 1 -> draftFormas.first().label
                                else -> "${draftFormas.size} seleccionadas"
                            },
                            modifier = Modifier.weight(1f),
                            color = Color.White,
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = m.selectSp.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(
                            imageVector = if (presentationExpanded) {
                                Icons.Filled.KeyboardArrowUp
                            } else {
                                Icons.Filled.KeyboardArrowDown
                            },
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    if (presentationExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(m.menuHeight)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                .padding(top = 2.dp, bottom = 2.dp),
                        ) {
                            listOf(
                                FormaFarmaceutica.COMPRIMIDOS to null,
                                FormaFarmaceutica.CAPSULAS to null,
                                FormaFarmaceutica.SUSPENSION to FormaFarmaceutica.OTROS,
                            ).forEach { (left, right) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(modifier = Modifier.weight(1.15f)) {
                                        val checked = left in draftFormas
                                        FilterSquareCheckboxRow(
                                            label = left.label,
                                            checked = checked,
                                            labelColor = LaSanteText,
                                            optionSp = m.optionSp,
                                            checkboxSize = m.checkboxSize,
                                            checkboxIconSize = m.checkboxIconSize,
                                            onToggle = {
                                                draftFormas = if (checked) {
                                                    draftFormas - left
                                                } else {
                                                    draftFormas + left
                                                }
                                            },
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (right != null) {
                                            val checked = right in draftFormas
                                            FilterSquareCheckboxRow(
                                                label = right.label,
                                                checked = checked,
                                                labelColor = LaSanteText,
                                                optionSp = m.optionSp,
                                                checkboxSize = m.checkboxSize,
                                                checkboxIconSize = m.checkboxIconSize,
                                                onToggle = {
                                                    draftFormas = if (checked) {
                                                        draftFormas - right
                                                    } else {
                                                        draftFormas + right
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = actionModifier
                            .clip(filterActionShape)
                            .background(FilterGreenBrush)
                            .clickable {
                                draftFormas = emptySet()
                                draftStarsOnly = false
                                presentationExpanded = false
                                onClear()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Limpiar filtros",
                            color = Color.White,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = m.buttonSp.sp,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Productos Estrellas",
                        modifier = Modifier.fillMaxWidth(),
                        style = filterSubtitleStyle.copy(fontSize = m.subtitleSp.sp),
                        textAlign = TextAlign.Start,
                    )
                    Text(
                        text = "Se mostrará únicamente los productos estrellas relacionados a esta unidad de negocio.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        style = filterSubtitleStyle.copy(
                            fontSize = m.helpSp.sp,
                            lineHeight = m.helpLineSp.sp,
                        ),
                        textAlign = TextAlign.Start,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                val stroke = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(10f, 8f),
                                        0f,
                                    ),
                                )
                                drawRoundRect(
                                    color = Color(0xFFD1D5DB),
                                    style = stroke,
                                    cornerRadius = CornerRadius(12.dp.toPx()),
                                )
                            }
                            .clickable { draftStarsOnly = !draftStarsOnly }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterSquareCheckbox(
                            checked = draftStarsOnly,
                            size = m.checkboxSize,
                            iconSize = m.checkboxIconSize,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Mostrar Productos estrellas",
                            color = FilterStarGold,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = m.selectSp.sp,
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = actionModifier
                            .clip(filterActionShape)
                            .background(FilterBlueBrush)
                            .clickable {
                                onApply(
                                    TherapeuticClassCatalogFilter(
                                        formas = draftFormas,
                                        starProductsOnly = draftStarsOnly,
                                    ),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Aplicar filtros",
                            color = Color.White,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = m.buttonSp.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSquareCheckboxRow(
    label: String,
    checked: Boolean,
    labelColor: Color,
    optionSp: Int,
    checkboxSize: Dp,
    checkboxIconSize: Dp,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterSquareCheckbox(
            checked = checked,
            size = checkboxSize,
            iconSize = checkboxIconSize,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = labelColor,
            fontSize = optionSp.sp,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Checkbox cuadrado del mock (borde verde; check blanco cuando está activo). */
@Composable
private fun FilterSquareCheckbox(
    checked: Boolean,
    size: Dp = 22.dp,
    iconSize: Dp = 16.dp,
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .border(
                width = 2.dp,
                color = if (checked) FilterGreenEnd else FilterGreenStart,
                shape = shape,
            )
            .background(if (checked) FilterGreenEnd else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    selectedFilter: ProductFilter,
    onFilterSelected: (ProductFilter) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    // skipPartiallyExpanded: abre expandido para ver Limpiar/Cerrar sin deslizar.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 4.dp, bottom = 28.dp),
        ) {
            Text("Filtros", style = MaterialTheme.typography.titleMedium, color = LaSanteGreen)
            Spacer(modifier = Modifier.height(12.dp))

            ProductFilter.entries.forEach { option ->
                FilterOptionRow(
                    text = option.label(),
                    selected = selectedFilter == option,
                    onClick = { onFilterSelected(option) },
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onClearFilters,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, LaSanteGreen)
            ) {
                Text("Limpiar filtros", color = LaSanteGreen, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LaSanteGreen),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text("Cerrar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FilterOptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = LaSanteGreen),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = LaSanteText,
            fontSize = 17.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            softWrap = true,
            maxLines = 2,
            overflow = TextOverflow.Clip,
            modifier = Modifier.weight(1f),
        )
    }
}

private data class ProductsGridScrollbar(
    val canScroll: Boolean,
    val scrollFraction: Float,
    val thumbFraction: Float,
)

/** Posición real del scroll (por filas/píxeles), no solo por índice de ítem. */
private fun computeProductsGridScrollbar(
    state: LazyGridState,
    columns: Int,
): ProductsGridScrollbar {
    val info = state.layoutInfo
    val totalItems = info.totalItemsCount
    val visible = info.visibleItemsInfo
    if (totalItems == 0 || visible.isEmpty()) {
        return ProductsGridScrollbar(canScroll = false, scrollFraction = 0f, thumbFraction = 1f)
    }

    val cols = columns.coerceAtLeast(1)
    val viewport = (info.viewportEndOffset - info.viewportStartOffset).toFloat().coerceAtLeast(1f)

    val rows = visible.groupBy { it.index / cols }.toSortedMap()
    val avgRowHeight = rows.values
        .map { items -> items.maxOf { it.size.height }.toFloat() }
        .average()
        .toFloat()
        .coerceAtLeast(1f)

    val rowKeys = rows.keys.toList()
    val rowSpacing = if (rowKeys.size >= 2) {
        val top0 = rows.getValue(rowKeys[0]).minOf { it.offset.y }
        val top1 = rows.getValue(rowKeys[1]).minOf { it.offset.y }
        val height0 = rows.getValue(rowKeys[0]).maxOf { it.size.height }
        (top1 - top0 - height0).toFloat().coerceAtLeast(0f)
    } else {
        0f
    }

    val totalRows = (totalItems + cols - 1) / cols
    val contentPadding = (info.beforeContentPadding + info.afterContentPadding).toFloat()
    val estimatedContent =
        totalRows * avgRowHeight +
            (totalRows - 1).coerceAtLeast(0) * rowSpacing +
            contentPadding
    val maxScroll = (estimatedContent - viewport).coerceAtLeast(0f)

    val first = visible.first()
    val firstRow = first.index / cols
    val scrolled =
        firstRow * (avgRowHeight + rowSpacing) -
            first.offset.y.toFloat() +
            info.beforeContentPadding.toFloat()

    val canScroll = maxScroll > 2f || state.canScrollForward || state.canScrollBackward
    val scrollFraction = if (maxScroll > 0f) {
        (scrolled / maxScroll).coerceIn(0f, 1f)
    } else {
        0f
    }
    val thumbFraction = if (estimatedContent > 0f) {
        (viewport / estimatedContent).coerceIn(0.12f, 1f)
    } else {
        1f
    }

    return ProductsGridScrollbar(
        canScroll = canScroll,
        scrollFraction = scrollFraction,
        thumbFraction = thumbFraction,
    )
}
