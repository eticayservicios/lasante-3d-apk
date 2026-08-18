package com.lasante.tvkiosk.ui.screens.treatments

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lasante.tvkiosk.BuildConfig
import com.lasante.tvkiosk.data.DisplayTitles
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.data.Treatment
import com.lasante.tvkiosk.ui.components.GreenNavButton
import com.lasante.tvkiosk.ui.components.LaSanteBackground
import com.lasante.tvkiosk.ui.components.ProductSearchBarMetrics
import com.lasante.tvkiosk.ui.components.SmartProductSearchBar
import com.lasante.tvkiosk.ui.components.TreatmentIconAssets
import com.lasante.tvkiosk.ui.components.TrimTransparentTransformation
import com.lasante.tvkiosk.ui.layout.CatalogHeaderMetrics
import com.lasante.tvkiosk.ui.layout.CatalogScreenTitle
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier
import com.lasante.tvkiosk.ui.layout.LogCatalogHeaderProfile
import com.lasante.tvkiosk.ui.layout.SharedNavMetrics
import com.lasante.tvkiosk.ui.layout.layoutForceOverlayLabel
import com.lasante.tvkiosk.ui.layout.rememberCatalogLayout
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import com.lasante.tvkiosk.ui.theme.LaSanteText
import com.lasante.tvkiosk.ui.utils.SoundManager
import com.lasante.tvkiosk.ui.utils.UiSound
import com.lasante.tvkiosk.ui.utils.clickableWithSound
import kotlin.math.roundToInt

@Composable
fun TreatmentsScreen(
    unitName: String,
    treatments: List<Treatment>,
    products: List<Product>,
    onBack: () -> Unit,
    onTreatmentSelected: (String) -> Unit,
    onProductSelected: (Product) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var searchSessionOpen by remember { mutableStateOf(false) }
    var searchEditing by remember { mutableStateOf(false) }
    var dismissListTick by remember { mutableStateOf(0) }
    fun dismissSearchSession() {
        if (searchEditing) {
            focusManager.clearFocus()
            keyboard?.hide()
        } else {
            dismissListTick += 1
        }
    }
    val gridState = rememberLazyGridState()
    val context = LocalContext.current
    BackHandler {
        when {
            searchEditing -> dismissSearchSession()
            searchSessionOpen -> dismissSearchSession()
            else -> {
                SoundManager.playClickSound(context)
                onBack()
            }
        }
    }

    LaSanteBackground {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val catalog = rememberCatalogLayout(maxWidth, maxHeight)
            val profile = catalog.profile
            val header = catalog.header
            val ui = catalog.ui
            LogCatalogHeaderProfile(header = header, screen = "CT")

            val columns = when {
                catalog.largeCanvas || profile.tier == DeviceProfileTier.TV_REGULAR -> 4
                else -> catalog.grid.columns
            }
            // Shared TV: spacing del grid. Phone / legacy: valores previos.
            val cardSpacing = when {
                header.usesSharedTvCatalogLayout -> catalog.grid.cardSpacing
                profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE -> 14.dp
                else -> 18.dp
            }
            // Distancias con regla en panel (1 cm ≈ 0.08H).
            // Back → subtítulo = 1 cm; subtítulo → cards = 0,5 cm.
            val cmOnCanvas = maxHeight * 0.08f
            val subtitleTopGap = cmOnCanvas
            val subtitleToGridGap = cmOnCanvas * 0.5f
            val ctCardWidthFraction = CatalogHeaderMetrics.ctCardWidthFraction(
                isTv66 = header.isTv66,
                isTv42 = header.isTv42,
                largeCanvas = header.isLargeCanvas,
            )
            val canvasW = maxWidth
            val canvasH = maxHeight
            val searchMetrics = ProductSearchBarMetrics.scaledForCanvas(canvasH)
            var overlayRootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
            var searchBarOffset by remember { mutableStateOf(IntOffset.Zero) }
            var searchBarPlaced by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { overlayRootCoordinates = it },
            ) {
                if (BuildConfig.DEBUG) {
                    Text(
                        text = "${canvasW.value.toInt()}×${canvasH.value.toInt()} · ${profile.tier} · " +
                            "large=${header.isLargeCanvas} · btn=${header.navButtonSize.value}dp · " +
                            "tv66=${header.isTv66} · fill=${"%.2f".format(ui.cardIconFill)} · " +
                            layoutForceOverlayLabel(),
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
                            .widthIn(max = catalog.gridMaxWidth)
                            .fillMaxWidth()
                            .padding(horizontal = catalog.horizontalPadding)
                            .zIndex(10f),
                    ) {
                        TreatmentsHeader(
                            unitName = unitName,
                            nav = catalog.nav,
                            header = header,
                            searchMetrics = searchMetrics,
                            contentPadding = catalog.contentPadding,
                            onBack = onBack,
                            onSearchBarPositioned = { coords ->
                                val root = overlayRootCoordinates
                                if (root == null || !root.isAttached || !coords.isAttached) return@TreatmentsHeader
                                val pos = root.localPositionOf(coords, Offset.Zero)
                                val next = IntOffset(pos.x.roundToInt(), pos.y.roundToInt())
                                if (next != searchBarOffset) {
                                    searchBarOffset = next
                                }
                                if (!searchBarPlaced) {
                                    searchBarPlaced = true
                                }
                            },
                            modifier = Modifier.zIndex(2f),
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .zIndex(0f),
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Spacer(modifier = Modifier.height(subtitleTopGap))

                                Text(
                            text = "Clase terapéutica",
                            fontSize = catalog.titleSp.sp,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                brush = Brush.horizontalGradient(
                                    listOf(LaSanteGreen, Color(0xFFA8C829)),
                                ),
                            ),
                            textAlign = TextAlign.End,
                            color = Color.Unspecified,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = catalog.contentPadding,
                                    end = catalog.contentPadding + if (header.usesSharedTvCatalogLayout) {
                                        header.navButtonSize + header.navPairSpacing
                                    } else {
                                        0.dp
                                    },
                                ),
                        )

                        Spacer(modifier = Modifier.height(subtitleToGridGap))

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            LazyVerticalGrid(
                                state = gridState,
                                columns = GridCells.Fixed(columns),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.TopCenter),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    start = catalog.contentPadding,
                                    end = catalog.contentPadding,
                                    top = CatalogHeaderMetrics.treatmentsGridTopPadding(
                                        sharedTv = header.usesSharedTvCatalogLayout,
                                    ),
                                    bottom = if (profile.isWide) 24.dp else 16.dp,
                                ),
                                verticalArrangement = Arrangement.spacedBy(cardSpacing),
                                horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                            ) {
                                items(
                                    items = treatments,
                                    key = { treatment -> treatment.id },
                                ) { treatment ->
                                    TherapeuticClassCard(
                                        treatment = treatment,
                                        iconSize = ui.cardIconSize,
                                        labelFontSize = ui.cardLabelFontSize,
                                        labelLineHeight = ui.cardLabelLineHeight,
                                        iconFill = ui.cardIconFill,
                                        aspectRatio = ui.cardAspectRatio,
                                        blockWidthFraction = ctCardWidthFraction,
                                        cardPaddingH = ui.cardPaddingH,
                                        cardPaddingTop = ui.cardPaddingTop,
                                        cardPaddingBottom = ui.cardPaddingBottom,
                                        cardIconPaddingH = ui.cardIconPaddingH,
                                        cardIconPaddingV = ui.cardIconPaddingV,
                                        onClick = {
                                            focusManager.clearFocus()
                                            onTreatmentSelected(treatment.id)
                                        },
                                    )
                                }
                            }
                        }
                            }
                        }
                    }
                }

                if (searchSessionOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(20.5f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { dismissSearchSession() },
                            ),
                    )
                }

                if (searchBarPlaced) {
                    Box(
                        modifier = Modifier
                            .offset { searchBarOffset }
                            .width(searchMetrics.width)
                            .wrapContentHeight(unbounded = true, align = Alignment.Top)
                            .zIndex(21f),
                    ) {
                        SmartProductSearchBar(
                            products = products,
                            onProductSelected = onProductSelected,
                            metrics = searchMetrics,
                            onActiveChange = { searchSessionOpen = it },
                            onEditingChange = { searchEditing = it },
                            dismissListTick = dismissListTick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TreatmentsHeader(
    unitName: String,
    nav: SharedNavMetrics,
    header: CatalogHeaderMetrics,
    searchMetrics: ProductSearchBarMetrics,
    contentPadding: Dp = 0.dp,
    onBack: () -> Unit,
    onSearchBarPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = contentPadding)
            .padding(top = if (header.usesSharedTvCatalogLayout) header.controlsTopGap else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CatalogScreenTitle(
            text = DisplayTitles.resolve(unitName),
            nav = nav,
            titleStartGap = header.titleStartGap,
            // Shared TV: título en la misma línea que Back.
            titleTopGap = if (header.usesSharedTvCatalogLayout) 0.dp else header.titleTopGap,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(searchMetrics.width)
                .height(searchMetrics.height)
                .onGloballyPositioned(onSearchBarPositioned),
        )
        Spacer(modifier = Modifier.width(header.searchToNavGap))
        // Misma X que Productos: Back + hueco de Home (aunque Home no esté).
        Row(
                horizontalArrangement = Arrangement.spacedBy(
                    if (header.usesSharedTvCatalogLayout) {
                        header.navPairSpacing
                    } else {
                        nav.buttonSpacing
                    },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GreenNavButton(
                    assetPath = "svg/ui/Before.svg",
                    contentDescription = "Volver",
                    onClick = onBack,
                    size = header.navButtonSize,
                    playSound = true,
                )
                Spacer(modifier = Modifier.size(header.navButtonSize))
            }
    }
}

@Composable
private fun TherapeuticClassCard(
    treatment: Treatment,
    iconSize: Dp,
    labelFontSize: TextUnit,
    labelLineHeight: TextUnit,
    iconFill: Float,
    aspectRatio: Float,
    blockWidthFraction: Float = 1f,
    cardPaddingH: Dp = 4.dp,
    cardPaddingTop: Dp = 4.dp,
    cardPaddingBottom: Dp = 2.dp,
    cardIconPaddingH: Dp = 6.dp,
    cardIconPaddingV: Dp = 4.dp,
    onClick: () -> Unit,
) {
    val iconModel = TreatmentIconAssets.resolve(iconUrl = treatment.media.icono)
    val context = LocalContext.current
    val iconSizePx = with(LocalDensity.current) { iconSize.roundToPx() }
    val labelHeight = therapeuticClassLabelHeight(labelLineHeight)
    val imageRequest = remember(iconModel, iconSizePx) {
        if (iconModel.isNullOrBlank()) null
        else ImageRequest.Builder(context)
            .data(iconModel)
            .size(iconSizePx)
            .transformations(TrimTransparentTransformation())
            .crossfade(true)
            .build()
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(blockWidthFraction)
                .aspectRatio(aspectRatio)
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(18.dp),
                    clip = false,
                )
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF4F4F4), Color(0xFFE1E1E1)),
                    ),
                )
                .clickableWithSound(sound = UiSound.Click, onClick = onClick)
                .padding(
                    start = cardPaddingH,
                    end = cardPaddingH,
                    top = cardPaddingTop,
                    bottom = cardPaddingBottom,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
                    .padding(horizontal = cardIconPaddingH, vertical = cardIconPaddingV),
                contentAlignment = Alignment.Center,
            ) {
                if (imageRequest != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = treatment.name,
                        modifier = Modifier.fillMaxSize(iconFill),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(labelHeight),
                contentAlignment = Alignment.TopCenter,
            ) {
                Text(
                    text = treatment.name,
                    color = LaSanteText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = labelFontSize,
                        fontWeight = FontWeight.Normal,
                        lineHeight = labelLineHeight,
                        textAlign = TextAlign.Center,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Altura del rótulo (~2 líneas) — evita truncate en TV42/TV42. */
@Composable
private fun therapeuticClassLabelHeight(lineHeight: TextUnit): Dp {
    return with(LocalDensity.current) { (lineHeight.toPx() * 2.1f).toDp() }
}
