package com.lasante.tvkiosk.ui.screens.treatments

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lasante.tvkiosk.data.Treatment
import com.lasante.tvkiosk.ui.components.GreenNavButton
import com.lasante.tvkiosk.ui.components.LaSanteBackground
import com.lasante.tvkiosk.ui.components.LaSanteScreenTitle
import com.lasante.tvkiosk.ui.components.TreatmentIconAssets
import com.lasante.tvkiosk.ui.layout.DeviceProfileResolver
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier
import com.lasante.tvkiosk.ui.layout.SharedNavMetrics
import com.lasante.tvkiosk.ui.layout.TvProfileDetector
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import com.lasante.tvkiosk.ui.theme.LaSanteText
import com.lasante.tvkiosk.data.DisplayTitles
import com.lasante.tvkiosk.ui.utils.clickableWithSound

@Composable
fun TreatmentsScreen(
    unitName: String,
    unitDescription: String,
    treatments: List<Treatment>,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onTreatmentSelected: (String) -> Unit,
) {
    BackHandler(onBack = onBack)

    val gridState = rememberLazyGridState()

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
            val grid = screenMetrics.grid
            val nav = screenMetrics.nav
            val uiMetrics = TreatmentUiMetrics.forProfile(screenMetrics.profile)
            val columns = grid.columns
            val horizontalPadding = grid.horizontalPadding
            val gridMaxWidth = grid.maxContentWidth
            val gridContentPadding = grid.contentPadding
            val topPadding = grid.topPadding
            val cardSpacing = grid.cardSpacing
            val profile = screenMetrics.profile

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
                TreatmentsHeader(
                    unitName = unitName,
                    navMetrics = nav,
                    navButtonSize = uiMetrics.navButtonSize,
                    contentPadding = gridContentPadding,
                    onBack = onBack,
                    onHome = onHome,
                )

                Spacer(modifier = Modifier.height(if (profile.isWide) 18.dp else 12.dp))

                Text(
                    text = "Clase terapéutica",
                    fontSize = when (profile.tier) {
                        DeviceProfileTier.TV_LARGE -> 34.sp
                        DeviceProfileTier.TV_REGULAR -> 26.sp
                        DeviceProfileTier.COMPACT_LANDSCAPE -> 18.sp
                        else -> if (profile.isWide) 28.sp else 20.sp
                    },
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
                        .padding(horizontal = gridContentPadding),
                )

                Spacer(modifier = Modifier.height(when (profile.tier) {
                    DeviceProfileTier.COMPACT_LANDSCAPE -> 10.dp
                    DeviceProfileTier.TV_REGULAR -> 16.dp
                    else -> if (profile.isWide) 26.dp else 18.dp
                }))

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.TopCenter),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = gridContentPadding,
                            end = gridContentPadding,
                            top = 8.dp,
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
                                iconSize = uiMetrics.cardIconSize,
                                labelFontSize = uiMetrics.cardLabelFontSize,
                                labelLineHeight = uiMetrics.cardLabelLineHeight,
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

@Composable
private fun TreatmentsHeader(
    unitName: String,
    navMetrics: SharedNavMetrics,
    navButtonSize: Dp,
    contentPadding: Dp = 0.dp,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LaSanteScreenTitle(
            text = DisplayTitles.resolve(unitName),
            fontSize = navMetrics.titleFontSize.value.toInt() + 2,
            textColor = LaSanteText,
            underlineBrush = Brush.horizontalGradient(
                listOf(Color(0xFF8FA88A), Color(0xFFD5D8D2), Color.White),
            ),
            underlineWidth = navMetrics.titleUnderlineWidth,
            underlineMatchTextWidth = true,
            textAlign = TextAlign.Start,
            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
            fontWeight = FontWeight.Light,
            allCaps = false,
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(navMetrics.buttonSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GreenNavButton(
                assetPath = "svg/ui/Before.svg",
                contentDescription = "Volver",
                onClick = onBack,
                size = navButtonSize,
            )
            GreenNavButton(
                assetPath = "svg/ui/Home.svg",
                contentDescription = "Inicio",
                onClick = onHome,
                size = navButtonSize,
            )
        }
    }
}

@Composable
private fun TherapeuticClassCard(
    treatment: Treatment,
    iconSize: Dp,
    labelFontSize: TextUnit,
    labelLineHeight: TextUnit,
    onClick: () -> Unit,
) {
    val iconModel = TreatmentIconAssets.resolve(
        id = treatment.id,
        name = treatment.name,
        iconUrl = treatment.media.icono,
    )
    val context = LocalContext.current
    val iconSizePx = with(LocalDensity.current) { iconSize.roundToPx() }

    LaunchedEffect(treatment.id, iconModel) {
        android.util.Log.d(
            "TreatmentIcon",
            "render id=${treatment.id} name=${treatment.name} icon=$iconModel",
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
            .clickableWithSound { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(iconModel)
                    .size(iconSizePx)
                    .crossfade(true)
                    .build(),
                contentDescription = treatment.name,
                modifier = Modifier.fillMaxSize(TreatmentUiMetrics.CARD_ICON_FILL),
                contentScale = ContentScale.Fit,
                onSuccess = {
                    android.util.Log.d(
                        "TreatmentIcon",
                        "loaded id=${treatment.id} icon=$iconModel",
                    )
                },
                onError = { state ->
                    android.util.Log.e(
                        "TreatmentIcon",
                        "error id=${treatment.id} icon=$iconModel throwable=${state.result.throwable}",
                    )
                },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(therapeuticClassLabelHeight(labelLineHeight)),
            contentAlignment = Alignment.Center,
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

/** Altura fija del rótulo (2 líneas) para que todos los cards del grid tengan el mismo alto. */
@Composable
private fun therapeuticClassLabelHeight(lineHeight: TextUnit): Dp {
    return with(LocalDensity.current) { (lineHeight.toPx() * 2f).toDp() }
}
