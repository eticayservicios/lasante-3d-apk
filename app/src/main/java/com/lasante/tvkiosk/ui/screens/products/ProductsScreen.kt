package com.lasante.tvkiosk.ui.screens.products

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.ui.components.GreenNavButton
import com.lasante.tvkiosk.ui.components.LaSanteBackground
import com.lasante.tvkiosk.ui.components.RealGreenScrollBar
import com.lasante.tvkiosk.ui.components.TreatmentIconAssets
import com.lasante.tvkiosk.ui.layout.DeviceProfileResolver
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier
import com.lasante.tvkiosk.ui.layout.TvProfileDetector
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
    data class Photo(val url: String, val enlarge: Boolean = false) : ProductGridVisual()
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
    val hasGlb = !media.modelo3d.glb.isNullOrBlank() ||
        !media.modelo3d.glbFrasco.isNullOrBlank() ||
        !media.modelo3d.glbAbrircaja.isNullOrBlank()
    return when {
        principal != null -> ProductGridVisual.Photo(principal)
        miniatura != null -> ProductGridVisual.Photo(
            url = miniatura,
            enlarge = hasGlb && principal == null,
        )
        preview != null -> ProductGridVisual.Photo(preview, enlarge = hasGlb)
        else -> ProductGridVisual.Placeholder
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    unitId: String,
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
            val preferTv66 = TvProfileDetector.isTv66Candidate(
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                density = LocalDensity.current,
                context = LocalContext.current,
            )
            val screenMetrics = DeviceProfileResolver.screenMetrics(
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                preferTv66 = preferTv66,
            )
            val profile = screenMetrics.profile
            val grid = screenMetrics.grid
            val nav = screenMetrics.nav
            val uiMetrics = TreatmentUiMetrics.forProfile(profile)
            val isLandscape = profile.isLandscape
            val isPhoneLandscape = profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE
            val isTv66 = profile.tier == DeviceProfileTier.TV_LARGE
            val isTv42 = profile.tier == DeviceProfileTier.TV_REGULAR
            val isTv = isTv42 || isTv66
            val columns = when (profile.tier) {
                DeviceProfileTier.TV_LARGE -> 3
                DeviceProfileTier.COMPACT_PORTRAIT -> 2
                else -> if (profile.isLandscape) 4 else 2
            }
            val buttonSize = uiMetrics.navButtonSize
            val gridHorizontalPadding = grid.horizontalPadding
            val gridContentStartPadding = grid.contentPadding
            val topPadding = grid.topPadding

            var searchQuery by remember { mutableStateOf("") }
            var sortOrder by remember { mutableStateOf(SortOrder.NONE) }
            var productFilter by remember { mutableStateOf(ProductFilter.ALL) }
            var showFilterSheet by remember { mutableStateOf(false) }
            var isSearching by remember { mutableStateOf(false) }
            var globalSearchResults by remember { mutableStateOf<List<Product>>(emptyList()) }

            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(searchQuery) {
                if (searchQuery.length >= 3) {
                    isSearching = true
                    kotlinx.coroutines.delay(500)
                    try {
                        val localResults = products.filter {
                            it.nombre.contains(searchQuery, ignoreCase = true) ||
                                it.descripcion.contains(searchQuery, ignoreCase = true)
                        }
                        if (localResults.isNotEmpty()) {
                            globalSearchResults = localResults
                        } else {
                            val result = catalogRepository.search(searchQuery, "productos")
                            globalSearchResults = result.items.map { item ->
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
                    } catch (e: Exception) {
                        globalSearchResults = products.filter {
                            it.nombre.contains(searchQuery, ignoreCase = true) ||
                                it.descripcion.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    isSearching = false
                } else {
                    globalSearchResults = emptyList()
                }
            }

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

            val filteredProducts = when (sortOrder) {
                SortOrder.AZ -> filteredByType.sortedBy { it.name }
                SortOrder.ZA -> filteredByType.sortedByDescending { it.name }
                SortOrder.NONE -> filteredByType
            }

            val gridState = rememberLazyGridState()

            val scrollInfo = remember {
                derivedStateOf {
                    val layoutInfo = gridState.layoutInfo
                    val totalItems = layoutInfo.totalItemsCount
                    val visibleItems = layoutInfo.visibleItemsInfo.size
                    if (totalItems <= visibleItems || totalItems == 0) 0f to 1f
                    else {
                        val firstVisibleIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                        val fraction = firstVisibleIndex.toFloat() /
                            (totalItems - visibleItems).coerceAtLeast(1).toFloat()
                        val thumbFraction = visibleItems.toFloat() / totalItems.toFloat()
                        fraction to thumbFraction
                    }
                }
            }

            val showScrollbar = filteredProducts.isNotEmpty()

            val badgeWidth = uiMetrics.badgeHeight * TreatmentUiMetrics.BADGE_WIDTH_TO_HEIGHT
            val badgeEndGap = when (profile.tier) {
                DeviceProfileTier.COMPACT_LANDSCAPE -> 18.dp
                DeviceProfileTier.TV_REGULAR -> 20.dp
                DeviceProfileTier.TV_LARGE -> 16.dp
                else -> 14.dp
            }
            val headerEndInsetFromBadge = badgeWidth + badgeEndGap
            val navStartPadding = gridContentStartPadding + when {
                isPhoneLandscape -> 10.dp
                isLandscape -> 8.dp
                else -> 4.dp
            }
            val searchBarWidth = when (profile.tier) {
                DeviceProfileTier.TV_REGULAR -> 320.dp
                DeviceProfileTier.TV_LARGE -> 360.dp
                DeviceProfileTier.COMPACT_LANDSCAPE -> 168.dp
                else -> if (profile.isLandscape) 220.dp else 150.dp
            }

            Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = gridHorizontalPadding,
                        end = when (profile.tier) {
                            DeviceProfileTier.TV_LARGE -> 24.dp
                            DeviceProfileTier.TV_REGULAR -> 16.dp
                            DeviceProfileTier.COMPACT_LANDSCAPE -> 10.dp
                            else -> if (profile.isLandscape) 16.dp else 16.dp
                        },
                        top = topPadding,
                        bottom = if (isLandscape) 6.dp else 8.dp,
                    ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.padding(start = navStartPadding),
                        horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 16.dp else 12.dp),
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
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = if (isPhoneLandscape) 6.dp else 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        ProductsTitle(
                            text = treatmentName,
                            isLandscape = isLandscape,
                            isTv42 = isTv42,
                            isPhoneLandscape = isPhoneLandscape,
                        )
                    }

                    Row(
                        modifier = Modifier.padding(
                            end = headerEndInsetFromBadge,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isPhoneLandscape) 6.dp else 8.dp),
                    ) {
                        AsyncImage(
                            model = "file:///android_asset/vitrina/ui/filter_button.png",
                            contentDescription = "Filtrar",
                            modifier = Modifier
                                .size(when {
                                    isTv42 -> 30.dp
                                    isPhoneLandscape -> 24.dp
                                    isLandscape -> 28.dp
                                    else -> 26.dp
                                })
                                .clickableWithSound { showFilterSheet = true },
                            contentScale = ContentScale.Fit,
                        )

                        Box(
                            modifier = Modifier
                                .width(searchBarWidth)
                                .height(when {
                                    isTv42 -> 32.dp
                                    isPhoneLandscape -> 28.dp
                                    isLandscape -> 30.dp
                                    else -> 28.dp
                                })
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
                                    modifier = Modifier.size(if (isLandscape) 16.dp else 14.dp),
                                    contentScale = ContentScale.Fit,
                                )
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    textStyle = TextStyle(
                                        color = LaSanteText,
                                        fontSize = if (isTv66) 13.sp else if (isLandscape) 12.sp else 11.sp,
                                    ),
                                    cursorBrush = SolidColor(LaSanteGreen),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "Buscar Producto",
                                                color = LaSanteTextSecondary,
                                                fontSize = if (isTv66) 13.sp else if (isLandscape) 12.sp else 11.sp,
                                            )
                                        }
                                        innerTextField()
                                    },
                                )
                                if (isSearching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = LaSanteGreen,
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = if (isPhoneLandscape) 4.dp else 5.dp,
                            end = headerEndInsetFromBadge,
                        ),
                    horizontalArrangement = Arrangement.End,
                ) {
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
                    )
                }
            }

                // Contenedor principal de la cuadrícula de productos SIN borde
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = gridHorizontalPadding),
                ) {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = gridContentStartPadding,
                            end = if (showScrollbar) (if (isLandscape) 48.dp else 40.dp) + 16.dp else 16.dp,
                            top = 16.dp,
                            bottom = 32.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(
                            count = filteredProducts.size,
                            key = { index -> filteredProducts[index].productoId },
                        ) { index ->
                            ProductGridItem(
                                product = filteredProducts[index],
                                isLandscape = isLandscape,
                                isPhoneLandscape = isPhoneLandscape,
                                isTv42 = isTv42,
                                isTv66 = isTv66,
                                onClick = { onProductSelected(filteredProducts[index]) },
                            )
                        }
                    }

                    if (showScrollbar) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(if (isLandscape) 48.dp else 40.dp)
                                .fillMaxHeight()
                                .padding(bottom = 24.dp, top = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            IconButton(onClick = {
                                coroutineScope.launch { gridState.animateScrollToItem(0) }
                            }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = LaSanteGreen)
                            }
                            RealGreenScrollBar(
                                scrollFraction = scrollInfo.value.first,
                                thumbFraction = scrollInfo.value.second,
                                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                            )
                            IconButton(onClick = {
                                coroutineScope.launch { gridState.animateScrollToItem(filteredProducts.size - 1) }
                            }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = LaSanteGreen)
                            }
                        }
                    }
                }
            }

            TreatmentIconBadge(
                iconUrl = treatmentIconUrl,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset {
                        IntOffset(
                            0,
                            if (isPhoneLandscape) (-10).dp.roundToPx() else if (isTv42) (-6).dp.roundToPx() else 0,
                        )
                    }
                    .padding(
                        top = when {
                            isPhoneLandscape -> 8.dp
                            isTv42 -> 12.dp
                            isLandscape -> 16.dp
                            else -> 10.dp
                        },
                        end = when {
                            isPhoneLandscape -> 10.dp
                            isTv -> 16.dp
                            isLandscape -> 12.dp
                            else -> 10.dp
                        },
                    ),
                isPhoneLandscape = isPhoneLandscape,
                isTv42 = isTv42,
                isTv = isTv,
                isTv66 = isTv66,
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
    modifier: Modifier = Modifier,
) {
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
                .padding(
                    horizontal = if (isTv66) 18.dp else if (isLandscape) 16.dp else 12.dp,
                    vertical = when {
                        isTv66 -> 4.dp
                        isTv42 -> 2.dp
                        isLandscape -> 2.dp
                        else -> 3.dp
                    },
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = when (sortOrder) {
                    SortOrder.NONE -> "Ordenar A - Z"
                    SortOrder.AZ -> "A - Z"
                    SortOrder.ZA -> "Z - A"
                },
                color = Color.White,
                fontSize = if (isTv66) 12.sp else if (isLandscape) 11.sp else 10.sp,
                fontWeight = FontWeight.Bold,
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(if (isTv66) 16.dp else if (isLandscape) 14.dp else 12.dp),
            )
        }
    }
}

@Composable
private fun ProductsTitle(
    text: String,
    isLandscape: Boolean,
    isTv42: Boolean = false,
    isPhoneLandscape: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val underlineBrush = Brush.horizontalGradient(listOf(LaSanteGreen, Color(0xFFC5E1A5)))
    val density = LocalDensity.current
    var underlineWidth by remember(text) { mutableStateOf(0.dp) }

    Column(
        modifier = modifier.wrapContentSize(align = Alignment.TopStart),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = text.uppercase(),
            onTextLayout = { layout ->
                underlineWidth = with(density) { layout.size.width.toDp() }
            },
            style = MaterialTheme.typography.headlineSmall.copy(
                brush = underlineBrush,
                fontSize = when {
                    isTv42 -> 25.sp
                    isPhoneLandscape -> 17.sp
                    isLandscape -> 25.sp
                    else -> 20.sp
                },
                letterSpacing = 0.sp,
            ),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false,
        )
        if (underlineWidth > 0.dp) {
            Box(
                modifier = Modifier
                    .width(underlineWidth)
                    .height(2.dp)
                    .background(underlineBrush, RoundedCornerShape(1.dp)),
            )
        }
    }
}

@Composable
private fun TreatmentIconBadge(
    iconUrl: String?,
    modifier: Modifier = Modifier,
    isPhoneLandscape: Boolean = false,
    isTv42: Boolean = false,
    isTv: Boolean = false,
    isTv66: Boolean = false,
) {
    val iconModel = TreatmentIconAssets.resolve(iconUrl = iconUrl)
    val metrics = TreatmentUiMetrics.profile(
        isPhoneLandscape = isPhoneLandscape,
        isTv66 = isTv66,
        isTv42 = isTv42,
        isTv = isTv,
    )
    val badgeHeight = metrics.badgeHeight
    val badgeWidth = badgeHeight * TreatmentUiMetrics.BADGE_WIDTH_TO_HEIGHT
    val iconSize = metrics.badgeIconSize
    val iconTopPadding = metrics.badgeIconTop

    Box(
        modifier = modifier
            .width(badgeWidth)
            .height(badgeHeight),
        contentAlignment = Alignment.TopCenter,
    ) {
        AsyncImage(
            model = "file:///android_asset/vitrina/ui/treatment_badge_shadow.png",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        if (!iconModel.isNullOrBlank()) {
            AsyncImage(
                model = iconModel,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = iconTopPadding.coerceAtLeast(0.dp))
                    .size(iconSize),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun ProductGridItem(
    product: Product,
    isLandscape: Boolean,
    isPhoneLandscape: Boolean = false,
    isTv42: Boolean = false,
    isTv66: Boolean = false,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val gridImageSizePx = with(density) {
        (if (isTv66) 224.dp else if (isLandscape) 152.dp else 124.dp).roundToPx()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 0.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Transparent)
            .clickableWithSound { onClick() }
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        // Imagen del producto
        val gridVisual = product.gridVisual()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTv66) 224.dp else if (isLandscape) 152.dp else 124.dp)
                .padding(
                    top = if (isTv66) 10.dp else 6.dp,
                    start = if (isTv66) 12.dp else 6.dp,
                    end = if (isTv66) 12.dp else 6.dp,
                    bottom = if (isTv66) 4.dp else 2.dp,
                )
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE7E7E7), Color(0xFFF7F7F7)),
                    ),
                )
                .padding(
                    horizontal = if (gridVisual is ProductGridVisual.Photo && gridVisual.enlarge) 0.dp else 4.dp,
                    vertical = if (gridVisual is ProductGridVisual.Photo && gridVisual.enlarge) 0.dp else 3.dp,
                ),
            contentAlignment = Alignment.Center
        ) {
            when (gridVisual) {
                is ProductGridVisual.Photo -> {
                    val enlargeScale = when {
                        !gridVisual.enlarge -> 1f
                        isTv66 -> 1.68f
                        isTv42 -> 1.60f
                        isPhoneLandscape -> 1.72f
                        isLandscape -> 1.55f
                        else -> 1.45f
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(gridVisual.url)
                            .size(gridImageSizePx)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(enlargeScale)
                            .align(Alignment.Center),
                        contentScale = if (gridVisual.enlarge) ContentScale.Crop else ContentScale.Fit,
                    )
                }
                ProductGridVisual.Placeholder -> Text(
                    "📦",
                    fontSize = if (isTv66) 86.sp else if (isLandscape) 64.sp else 54.sp,
                    color = Color.Gray,
                )
            }
        }

        // Contenedor para el texto del producto con borde verde ligero (sin fondo blanco)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .border(BorderStroke(1.dp, LaSanteGreen.copy(alpha = 0.45f)), RoundedCornerShape(50.dp))
                .clip(RoundedCornerShape(50.dp))
                .background(Color.Transparent)
                .padding(
                    horizontal = if (isTv66) 22.dp else 16.dp,
                    vertical = if (isTv66) 11.dp else 8.dp,
                )
                .align(Alignment.CenterHorizontally)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = product.name,
                    color = Color.Black,
                    fontSize = if (isTv66) 18.sp else if (isLandscape) 15.sp else 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                )

                // Subtítulo/Descripción del producto
                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        color = Color.Black.copy(alpha = 0.7f),
                        fontSize = if (isTv66) 14.sp else if (isLandscape) 12.sp else 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center, // Alinear el subtitulo al centro
                        lineHeight = if (isTv66) 17.sp else if (isLandscape) 14.sp else 12.sp,
                    )
                }
            }
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
