package com.lasante.tvkiosk.ui.screens.products

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import com.lasante.tvkiosk.data.CatalogRepository
import com.lasante.tvkiosk.data.DisplayTitles
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.ui.components.GreenNavButton
import com.lasante.tvkiosk.ui.components.LaSanteBackground
import com.lasante.tvkiosk.ui.components.LaSanteScreenTitle
import com.lasante.tvkiosk.ui.components.RealGreenScrollBar
import com.lasante.tvkiosk.ui.components.TreatmentIconAssets
import com.lasante.tvkiosk.ui.components.TrimTransparentTransformation
import com.lasante.tvkiosk.ui.layout.DeviceProfileResolver
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier
import com.lasante.tvkiosk.ui.layout.FireTv42Spacing
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
            val canvasWidth = maxWidth
            val canvasHeight = maxHeight
            val preferTv66 = TvProfileDetector.isTv66Candidate(
                maxWidth = canvasWidth,
                maxHeight = canvasHeight,
                density = LocalDensity.current,
                context = LocalContext.current,
            )
            val screenMetrics = DeviceProfileResolver.screenMetrics(
                maxWidth = canvasWidth,
                maxHeight = canvasHeight,
                preferTv66 = preferTv66,
            )
            val profile = screenMetrics.profile
            val grid = screenMetrics.grid
            val nav = screenMetrics.nav
            val isLandscape = profile.isLandscape
            val isPhoneLandscape = profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE
            val isTv66 = profile.tier == DeviceProfileTier.TV_LARGE
            val isTv42 = profile.tier == DeviceProfileTier.TV_REGULAR
            // Damasco / canvas alto (~1333×800) y TV 66"+ — UI e iconos más grandes.
            val isTv42LargeUp = (isTv42 && canvasHeight >= 700.dp) || isTv66
            val uiMetrics = TreatmentUiMetrics.forProfile(profile, largeCanvas = isTv42LargeUp)
            val isTv = isTv42 || isTv66
            // Fire / Television_1080 (no Damasco / TV66).
            val isFireTv42 = isTv42 && !isTv42LargeUp
            val isPhone = profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE ||
                profile.tier == DeviceProfileTier.COMPACT_PORTRAIT
            // Mínimo 4 productos por fila en landscape / TV.
            val columns = when {
                profile.tier == DeviceProfileTier.COMPACT_PORTRAIT -> 2
                else -> if (profile.isLandscape) 4 else 2
            }
            // Back/Home: tamaño SharedNav (versión anterior). tv42Large 52.dp los sacaba de pantalla.
            val buttonSize = when {
                isTv66 -> uiMetrics.navButtonSize
                isTv42 -> nav.buttonSize
                else -> uiMetrics.navButtonSize
            }
            val topPadding = grid.topPadding

            LaunchedEffect(profile.tier, canvasWidth, canvasHeight, isTv42LargeUp) {
                android.util.Log.i(
                    "ProductsProfile",
                    "tier=${profile.tier} size=${canvasWidth}x${canvasHeight} " +
                        "isTv42=$isTv42 largeUp=$isTv42LargeUp fire=$isFireTv42 " +
                        "btn=$buttonSize searchMax=${
                            when {
                                isTv66 -> 480.dp
                                isTv42LargeUp -> 280.dp
                                isTv42 -> 240.dp
                                isPhoneLandscape -> 196.dp
                                profile.isLandscape -> 256.dp
                                else -> 175.dp
                            }
                        }",
                )
            }

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

            val scrollInfo = remember(columns) {
                derivedStateOf { computeProductsGridScrollbar(gridState, columns) }
            }

            val showScrollbar = filteredProducts.isNotEmpty()

            val horizontalPadding = grid.horizontalPadding
            val contentPadding = grid.contentPadding
            val gridMaxWidth = grid.maxContentWidth
            val badgeWidth = uiMetrics.badgeHeight * TreatmentUiMetrics.BADGE_WIDTH_TO_HEIGHT
            val titleStartGap = badgeWidth + when {
                // Infinix: título más a la derecha (badge intacto).
                isPhoneLandscape -> 48.dp
                isTv42LargeUp -> 44.dp
                isTv66 -> 40.dp
                // Fire: misma corrección que CT (−2 espacios).
                isFireTv42 -> 34.dp - FireTv42Spacing.spaces(2)
                isTv42 -> 34.dp
                else -> 32.dp
            }
            val searchBarWidth = when {
                isTv66 -> 480.dp
                // Canvas alto / tablet: más angosto para no empujar Back/Home fuera.
                isTv42LargeUp -> 280.dp
                isTv42 -> 240.dp
                isPhoneLandscape -> 196.dp
                profile.isLandscape -> 256.dp
                else -> 175.dp
            }
            val filterToSearchGap = when {
                isPhoneLandscape -> 10.dp
                isTv42LargeUp -> 16.dp
                // Fire: buscar/ordenar +3 espacios a la derecha.
                isFireTv42 -> 14.dp + FireTv42Spacing.spaces(3)
                isTv42 -> 14.dp
                else -> 12.dp
            }
            val searchToNavGap = when {
                isPhoneLandscape -> 12.dp
                isTv42LargeUp -> 16.dp
                isTv42 -> 12.dp
                else -> 14.dp
            }
            val sortTopGap = when {
                isPhoneLandscape -> 4.dp
                isTv42LargeUp -> 12.dp
                else -> 5.dp
            }
            val searchBarHeight = when {
                isTv42LargeUp -> 44.dp
                isTv42 -> 32.dp
                isPhoneLandscape -> 28.dp
                isLandscape -> 30.dp
                else -> 28.dp
            }
            val filterIconSize = when {
                isTv42LargeUp -> 44.dp
                // Fire: filtro −5%.
                isFireTv42 -> 30.dp * 0.95f
                isTv42 -> 30.dp
                isPhoneLandscape -> 24.dp
                isLandscape -> 28.dp
                else -> 26.dp
            }
            val searchIconSize = when {
                isTv42LargeUp -> 22.dp
                isLandscape -> 16.dp
                else -> 14.dp
            }
            val searchFontSize = when {
                isTv42LargeUp -> 15.sp
                isTv66 -> 13.sp
                isLandscape -> 12.sp
                else -> 11.sp
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topPadding),
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
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LaSanteScreenTitle(
                                text = DisplayTitles.resolve(treatmentName),
                                fontSize = nav.titleFontSize.value.toInt() + 2,
                                textColor = LaSanteText,
                                underlineBrush = Brush.horizontalGradient(
                                    listOf(Color(0xFF8FA88A), Color(0xFFD5D8D2), Color.White),
                                ),
                                underlineWidth = nav.titleUnderlineWidth,
                                underlineMatchTextWidth = true,
                                textAlign = TextAlign.Start,
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                fontWeight = FontWeight.Light,
                                allCaps = false,
                                // weight: el título cede espacio; Back/Home nunca se cortan.
                                modifier = Modifier
                                    .padding(start = titleStartGap)
                                    .weight(1f),
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val filterContext = LocalContext.current
                                AsyncImage(
                                    model = "file:///android_asset/vitrina/ui/filter_button.png",
                                    contentDescription = "Filtrar",
                                    modifier = Modifier
                                        .size(filterIconSize)
                                        .combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                com.lasante.tvkiosk.ui.utils.SoundManager.playClickSound(filterContext)
                                                showFilterSheet = true
                                            },
                                            onLongClick = {
                                                val msg =
                                                    "${canvasWidth.value.toInt()}×${canvasHeight.value.toInt()} dp · " +
                                                        "${profile.tier} · large=$isTv42LargeUp · btn=$buttonSize"
                                                Toast.makeText(filterContext, msg, Toast.LENGTH_LONG).show()
                                                android.util.Log.i("ProductsProfile", msg)
                                            },
                                        ),
                                    contentScale = ContentScale.Fit,
                                )

                                Spacer(modifier = Modifier.width(filterToSearchGap))

                                // Buscador + Ordenar: misma columna; ancho tope, puede encogerse.
                                Column(
                                    modifier = Modifier.widthIn(max = searchBarWidth),
                                    horizontalAlignment = Alignment.End,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(searchBarWidth)
                                            .height(searchBarHeight)
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
                                                modifier = Modifier.size(searchIconSize),
                                                contentScale = ContentScale.Fit,
                                            )
                                            BasicTextField(
                                                value = searchQuery,
                                                onValueChange = { searchQuery = it },
                                                textStyle = TextStyle(
                                                    color = LaSanteText,
                                                    fontSize = searchFontSize,
                                                ),
                                                cursorBrush = SolidColor(LaSanteGreen),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                decorationBox = { innerTextField ->
                                                    if (searchQuery.isEmpty()) {
                                                        Text(
                                                            "Buscar Producto",
                                                            color = LaSanteTextSecondary,
                                                            fontSize = searchFontSize,
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
                                        modifier = Modifier.padding(top = sortTopGap),
                                    )
                                }

                                Spacer(modifier = Modifier.width(searchToNavGap))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(nav.buttonSpacing),
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
                            }
                        }

                        // Scroll en la grilla (como antes), centrado bajo el botón Back (mockup).
                        val scrollRailWidth = when {
                            isTv42LargeUp -> 48.dp
                            isLandscape -> 36.dp
                            else -> 32.dp
                        }
                        val scrollArrowSize = when {
                            isTv42LargeUp -> 36.dp
                            else -> 24.dp
                        }
                        val scrollEndInset = contentPadding + buttonSize + nav.buttonSpacing +
                            (buttonSize - scrollRailWidth) / 2
                        // Fire: scroll +7 espacios a la derecha (menos inset desde el borde).
                        val scrollEndPad = if (isFireTv42) {
                            (scrollEndInset - FireTv42Spacing.spaces(7)).coerceAtLeast(0.dp)
                        } else {
                            scrollEndInset
                        }

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
                                    top = 16.dp,
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

                // Badge pegado al top (como antes), a la izquierda; título queda separado vía titleStartGap.
                TreatmentIconBadge(
                    iconUrl = treatmentIconUrl,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            IntOffset(
                                // Fire: badge −2 espacios (izquierda).
                                x = if (isFireTv42) {
                                    (-FireTv42Spacing.spaces(2)).roundToPx()
                                } else {
                                    0
                                },
                                // Infinix: sin pull-up (como antes del ajuste del header).
                                y = if (isTv42) (-6).dp.roundToPx() else 0,
                            )
                        }
                        .padding(
                            start = horizontalPadding + contentPadding,
                            top = 0.dp,
                        ),
                    isPhoneLandscape = isPhoneLandscape,
                    isTv42 = isTv42,
                    isTv = isTv,
                    isTv66 = isTv66,
                    isTv42LargeUp = isTv42LargeUp,
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
                    horizontal = when {
                        isTv42LargeUp -> 28.dp
                        isTv66 -> 22.dp
                        isLandscape -> 20.dp
                        else -> 16.dp
                    },
                    vertical = when {
                        isTv42LargeUp -> 3.dp
                        isTv66 -> 2.dp
                        isTv42 -> 1.dp
                        isLandscape -> 1.dp
                        else -> 2.dp
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
                fontSize = when {
                    isTv42LargeUp -> 14.sp
                    isTv66 -> 12.sp
                    isLandscape -> 11.sp
                    else -> 10.sp
                },
                fontWeight = FontWeight.Bold,
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(
                    when {
                        isTv42LargeUp -> 20.dp
                        isTv66 -> 16.dp
                        isLandscape -> 14.dp
                        else -> 12.dp
                    },
                ),
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
    isTv42LargeUp: Boolean = false,
) {
    val iconModel = TreatmentIconAssets.resolve(iconUrl = iconUrl)
    val metrics = when {
        isTv42LargeUp && isTv66 -> TreatmentUiMetrics.tv66
        isTv42LargeUp -> TreatmentUiMetrics.tv42Large
        else -> TreatmentUiMetrics.profile(
            isPhoneLandscape = isPhoneLandscape,
            isTv66 = isTv66,
            isTv42 = isTv42,
            isTv = isTv,
        )
    }
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
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    // Cuadro gris cuadrado (no rectangular). El alto sigue al ancho de la celda.
    val imageFillFraction = when {
        isPhone -> 0.604f
        isTv42LargeUp -> 0.82f
        isTv42 -> 0.78f
        isTv66 -> 0.88f
        else -> 0.85f
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
    val titleBrush = Brush.horizontalGradient(
        listOf(LaSanteGreenDark, LaSanteGreen, Color(0xFFA8C829)),
    )
    val (titlePart, strengthPart) = remember(product.name) { splitProductTitleAndStrength(product.name) }
    val boxPad = when {
        isTv42LargeUp || isTv66 -> 8.dp
        else -> 4.dp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Transparent)
            .clickableWithSound { onClick() }
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        val gridVisual = product.gridVisual()
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(
                    top = boxPad,
                    start = if (isTv42LargeUp || isTv66) 10.dp else 6.dp,
                    end = if (isTv42LargeUp || isTv66) 10.dp else 6.dp,
                    bottom = if (isTv42LargeUp || isTv66) 4.dp else 2.dp,
                )
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE7E7E7), Color(0xFFF7F7F7)),
                    ),
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            val gridImageSizePx = with(density) {
                (minOf(maxWidth, maxHeight) * imageFillFraction).roundToPx()
            }
            when (gridVisual) {
                is ProductGridVisual.Photo -> {
                    // Misma escala Fit para miniatura y vista previa GLB.
                    // Phone: 50% del área del card; el contenedor gris se mantiene.
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(gridVisual.url)
                            .size(gridImageSizePx)
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
        }
    }
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
