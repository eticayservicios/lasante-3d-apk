package com.lasante.tvkiosk.ui.screens.treatments

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lasante.tvkiosk.BuildConfig
import com.lasante.tvkiosk.data.DisplayTitles
import com.lasante.tvkiosk.data.Treatment
import com.lasante.tvkiosk.ui.components.GreenNavButton
import com.lasante.tvkiosk.ui.components.LaSanteBackground
import com.lasante.tvkiosk.ui.components.TreatmentIconAssets
import com.lasante.tvkiosk.ui.components.TrimTransparentTransformation
import com.lasante.tvkiosk.ui.layout.CatalogHeaderMetrics
import com.lasante.tvkiosk.ui.layout.CatalogScreenTitle
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier
import com.lasante.tvkiosk.ui.layout.FireTv42Spacing
import com.lasante.tvkiosk.ui.layout.HikvisionLayoutDebug
import com.lasante.tvkiosk.ui.layout.LogCatalogHeaderProfile
import com.lasante.tvkiosk.ui.layout.SharedNavMetrics
import com.lasante.tvkiosk.ui.layout.rememberCatalogLayout
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import com.lasante.tvkiosk.ui.theme.LaSanteText
import com.lasante.tvkiosk.ui.utils.SoundManager

@Composable
fun TreatmentsScreen(
    unitName: String,
    treatments: List<Treatment>,
    onBack: () -> Unit,
    onTreatmentSelected: (String) -> Unit,
) {
    val gridState = rememberLazyGridState()
    val context = LocalContext.current
    BackHandler {
        SoundManager.playClickSound(context)
        onBack()
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
            // Shared TV: subir subtítulo (TV66 −2 esp; Fire/Ariana −1 esp).
            val subtitleTopGap = when {
                header.isTv66 -> (26.dp - FireTv42Spacing.spaces(2)).coerceAtLeast(8.dp)
                header.usesSharedTvCatalogLayout ->
                    (26.dp - FireTv42Spacing.spaces(1)).coerceAtLeast(10.dp)
                profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE -> 14.dp
                else -> if (profile.isWide) 18.dp else 14.dp
            }
            val subtitleToGridGap = when {
                header.usesSharedTvCatalogLayout -> 26.dp
                profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE -> 14.dp
                else -> if (profile.isWide) 18.dp else 14.dp
            }
            val ctCardWidthFraction = CatalogHeaderMetrics.ctCardWidthFraction(
                isTv66 = header.isTv66,
                isTv42 = header.isTv42,
                largeCanvas = header.isLargeCanvas,
            )
            val canvasW = maxWidth
            val canvasH = maxHeight

            Box(modifier = Modifier.fillMaxSize()) {
                if (BuildConfig.DEBUG) {
                    Text(
                        text = "${canvasW.value.toInt()}×${canvasH.value.toInt()} · ${profile.tier} · " +
                            "large=${header.isLargeCanvas} · btn=${header.navButtonSize.value}dp · " +
                            "tv66=${header.isTv66} · ${HikvisionLayoutDebug.overlayLabel()}",
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
                            .padding(horizontal = catalog.horizontalPadding),
                    ) {
                        TreatmentsHeader(
                            unitName = unitName,
                            nav = catalog.nav,
                            header = header,
                            contentPadding = catalog.contentPadding,
                            onBack = onBack,
                        )

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
                                    // TV66: alinear con el back (el header deja un Spacer=nav a la derecha).
                                    end = catalog.contentPadding + if (header.isTv66) {
                                        header.navButtonSize
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
                                        onClick = { onTreatmentSelected(treatment.id) },
                                    )
                                }
                            }
                        }
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
    contentPadding: Dp = 0.dp,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
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
                .clickable(onClick = onClick)
                .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
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

/** Altura del rótulo (~2 líneas) — evita truncate en TV42/Fire. */
@Composable
private fun therapeuticClassLabelHeight(lineHeight: TextUnit): Dp {
    return with(LocalDensity.current) { (lineHeight.toPx() * 2.1f).toDp() }
}
