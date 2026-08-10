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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.lasante.tvkiosk.ui.components.RealGreenScrollBar
import com.lasante.tvkiosk.ui.components.TreatmentIconAssets
import com.lasante.tvkiosk.ui.components.TrimTransparentTransformation
import com.lasante.tvkiosk.ui.layout.CatalogHeaderMetrics
import com.lasante.tvkiosk.ui.layout.CatalogScreenTitle
import com.lasante.tvkiosk.ui.layout.HikvisionLayoutDebug
import com.lasante.tvkiosk.ui.layout.LogCatalogHeaderProfile
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier
import com.lasante.tvkiosk.ui.layout.rememberCatalogLayout
import com.lasante.tvkiosk.ui.screens.treatments.TreatmentUiMetrics
import com.lasante.tvkiosk.ui.theme.*
import com.lasante.tvkiosk.ui.utils.clickableWithSound
import kotlinx.coroutines.launch

private enum class SortOrder { NONE, AZ, ZA }
private enum class ProductFilter { ALL, WITH_IMAGE, WITHOUT_IMAGE }

private fun Product.hasImage(): Boolean =
    media.modelo3d.vistaPrevia != null ||
        media.imagenes2d.principal != null ||
        media.imagenes2d.miniatura != null

private sealed class ProductGridVisual {
    data class Photo(val url: String) : ProductGridVisual()
    data object Placeholder : ProductGridVisual()
}

/**
 * Grilla: solo imágenes 2D (SceneView en LazyGrid rompe clics y se monta sobre el header).
 * GLB solo en el modal al abrir el producto.
 */
private fun Product.gridVisual(): ProductGridVisual {
    val principal = media.imagenes2d.principal?.trim()?.takeIf { it.isNotBlank() }
    val miniatura = media.imagenes2d.miniatura?.trim()?.takeIf { it.isNotBlank() }
    val preview = media.modelo3d.vistaPrevia?.trim()?.takeIf { it.isNotBlank() }
    return when {
        principal != null -> ProductGridVisual.Photo(principal)
        miniatura != null -> ProductGridVisual.Photo(miniatura)
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
            LogCatalogHeaderProfile(header = header, screen = "Products")

            var searchQuery by remember { mutableStateOf("") }
            var sortOrder by remember { mutableStateOf(SortOrder.NONE) }
            var productFilter by remember { mutableStateOf(ProductFilter.ALL) }
            var showFilterSheet by remember { mutableStateOf(false) }
            var isSearching by remember { mutableStateOf(false) }
            var globalSearchResults by remember { mutableStateOf<List<Product>>(emptyList()) }

            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(searchQuery, products) {
                if (searchQuery.length < 3) {
                    globalSearchResults = emptyList()
                    isSearching = false
                    return@LaunchedEffect
                }
                isSearching = true
                kotlinx.coroutines.delay(500)
                val query = searchQuery
                try {
                    val localResults = products.filter {
                        it.nombre.contains(query, ignoreCase = true) ||
                            it.descripcion.contains(query, ignoreCase = true)
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
                            )
                        }
                    }
                } catch (_: Exception) {
                    globalSearchResults = products.filter {
                        it.nombre.contains(query, ignoreCase = true) ||
                            it.descripcion.contains(query, ignoreCase = true)
                    }
                }
                isSearching = false
            }

            val filteredProducts = remember(
                products,
                searchQuery,
                globalSearchResults,
                productFilter,
                sortOrder,
            ) {
                val displayProducts = if (searchQuery.length >= 3) {
                    globalSearchResults
                } else {
                    products.filter {
                        searchQuery.isBlank() ||
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.description.contains(searchQuery, ignoreCase = true)
                    }
                }
                val filteredByType = when (productFilter) {
                    ProductFilter.ALL -> displayProducts
                    ProductFilter.WITH_IMAGE -> displayProducts.filter { it.hasImage() }
                    ProductFilter.WITHOUT_IMAGE -> displayProducts.filterNot { it.hasImage() }
                }
                when (sortOrder) {
                    SortOrder.AZ -> filteredByType.sortedBy { it.name }
                    SortOrder.ZA -> filteredByType.sortedByDescending { it.name }
                    SortOrder.NONE -> filteredByType
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
                if (BuildConfig.DEBUG) {
                    Text(
                        text = "${canvasWidth.value.toInt()}×${canvasHeight.value.toInt()} · ${profile.tier} · " +
                            "large=${header.isLargeCanvas} · btn=${buttonSize.value}dp · " +
                            "filter=${header.filterIconSize.value}dp · " +
                            HikvisionLayoutDebug.overlayLabel() +
                            if (header.isTv66 || profile.tier.name.contains("LARGE")) {
                                " · TV66-ref=1280×720"
                            } else {
                                ""
                            },
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
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
                                val density = LocalDensity.current
                                val filterSize = header.filterIconSize
                                val filterSizePx = with(density) { filterSize.roundToPx().coerceAtLeast(1) }
                                val filterTopPad = header.centerOnSearchBar(filterSize) + header.controlsTopGap
                                Box(
                                    modifier = Modifier
                                        .padding(top = filterTopPad)
                                        .offset(x = header.filterOffsetX)
                                        .size(filterSize)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { showFilterSheet = true },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(filterContext)
                                            .data("file:///android_asset/vitrina/ui/filter_button.png")
                                            .size(filterSizePx)
                                            .transformations(TrimTransparentTransformation())
                                            .build(),
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
                                val navSpacing =
                                    if (isTv66) header.navPairSpacing else nav.buttonSpacing
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
                                    if (isTv42LargeUp) 22.dp else 16.dp,
                                ),
                                horizontalArrangement = Arrangement.spacedBy(
                                    if (isTv42LargeUp) 22.dp else 16.dp,
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
                        }
                    }
                }

                // Badge top-start; título separado vía titleStartGap.
                TreatmentIconBadge(
                    iconUrl = treatmentIconUrl,
                    metrics = catalog.ui,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = if (isTv42) (-6).dp.roundToPx() else 0,
                            )
                        }
                        .padding(
                            start = horizontalPadding + contentPadding,
                            top = 0.dp,
                        ),
                )
            }

            if (showFilterSheet) {
                FilterBottomSheet(
                    selectedFilter = productFilter,
                    onFilterSelected = { productFilter = it },
                    onClearFilters = {
                        productFilter = ProductFilter.ALL
                        searchQuery = ""
                        sortOrder = SortOrder.NONE
                    },
                    onDismiss = { showFilterSheet = false },
                )
            }
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
    val iconModel = TreatmentIconAssets.resolve(iconUrl = iconUrl)
    val badgeHeight = metrics.badgeHeight
    val badgeWidth = badgeHeight * TreatmentUiMetrics.BADGE_WIDTH_TO_HEIGHT
    val iconSize = metrics.badgeIconSize

    Box(
        modifier = modifier
            .width(badgeWidth)
            .height(badgeHeight),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = "file:///android_asset/vitrina/ui/treatment_badge_shadow.png",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        if (!iconModel.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(iconModel)
                    .transformations(TrimTransparentTransformation())
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
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
    val (titlePart, strengthPart) = remember(product.name) { splitProductTitleAndStrength(product.name) }
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
                    .clickableWithSound { onClick() }
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

/**
 * Separa nombre de concentración/presentación.
 * Ej: "Cetirizina 10 mg" → ("Cetirizina", "10 mg");
 *     "Fexofenadina - Suspensión" → ("Fexofenadina", "Suspensión").
 */
private fun splitProductTitleAndStrength(rawName: String): Pair<String, String?> {
    val name = rawName.trim()
    if (name.isEmpty()) return "" to null

    val dosageMatch = PRODUCT_DOSAGE_SUFFIX.find(name)
    if (dosageMatch != null) {
        val title = dosageMatch.groupValues[1].trim().trimEnd('-', '–', '—').trim()
        val strength = dosageMatch.groupValues[2].trim()
        if (title.isNotEmpty()) return title to strength
    }

    val dashIndex = name.lastIndexOf(" - ")
    if (dashIndex > 0) {
        val title = name.substring(0, dashIndex).trim()
        val strength = name.substring(dashIndex + 3).trim()
        if (title.isNotEmpty() && strength.isNotEmpty()) return title to strength
    }

    return name to null
}

private val PRODUCT_DOSAGE_SUFFIX = Regex(
    """^(.+?)\s+(\d+[.,]?\d*\s*(?:mg|g|ml|mcg|µg|ui|%))\s*$""",
    RegexOption.IGNORE_CASE,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    selectedFilter: ProductFilter,
    onFilterSelected: (ProductFilter) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 16.dp)) {
            Text("Filtros", style = MaterialTheme.typography.titleMedium, color = LaSanteGreen)
            Spacer(modifier = Modifier.height(16.dp))

            FilterOptionRow(
                text = "Todos los productos",
                selected = selectedFilter == ProductFilter.ALL,
                onClick = { onFilterSelected(ProductFilter.ALL) }
            )
            FilterOptionRow(
                text = "Con imagen",
                selected = selectedFilter == ProductFilter.WITH_IMAGE,
                onClick = { onFilterSelected(ProductFilter.WITH_IMAGE) }
            )
            FilterOptionRow(
                text = "Sin imagen",
                selected = selectedFilter == ProductFilter.WITHOUT_IMAGE,
                onClick = { onFilterSelected(ProductFilter.WITHOUT_IMAGE) }
            )

            Spacer(modifier = Modifier.height(18.dp))
            OutlinedButton(
                onClick = onClearFilters,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, LaSanteGreen)
            ) {
                Text("Limpiar filtros", color = LaSanteGreen, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
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
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = LaSanteGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = LaSanteText,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
