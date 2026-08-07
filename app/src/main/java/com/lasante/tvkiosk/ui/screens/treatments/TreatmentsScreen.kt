package com.lasante.tvkiosk.ui.screens.treatments

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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lasante.tvkiosk.data.DisplayTitles
import com.lasante.tvkiosk.data.Treatment
import com.lasante.tvkiosk.ui.components.GreenNavButton
import com.lasante.tvkiosk.ui.components.LaSanteBackground
import com.lasante.tvkiosk.ui.components.LaSanteScreenTitle
import com.lasante.tvkiosk.ui.components.TreatmentIconAssets
import com.lasante.tvkiosk.ui.components.TrimTransparentTransformation
import com.lasante.tvkiosk.ui.layout.DeviceProfileResolver
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier
import com.lasante.tvkiosk.ui.layout.SharedNavMetrics
import com.lasante.tvkiosk.ui.layout.TvProfileDetector
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import com.lasante.tvkiosk.ui.theme.LaSanteText
import com.lasante.tvkiosk.ui.utils.clickableWithSound

@Composable
fun TreatmentsScreen(
    unitName: String,
    unitDescription: String,
    treatments: List<Treatment>,
    onBack: () -> Unit,
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
            val profile = screenMetrics.profile
            // Damasco (~1333×800) / TV 66 — mockup grande. Fire (~961×529) e Infinix = baseline.
            val isLargeCanvas =
                profile.tier == DeviceProfileTier.TV_LARGE ||
                    (profile.tier == DeviceProfileTier.TV_REGULAR && maxHeight >= 700.dp)
            val uiMetrics = TreatmentUiMetrics.forProfile(profile, largeCanvas = isLargeCanvas)
            // Mockup grande / Fire TV42 = 4 cols (nombres legibles). Infinix mantiene 5.
            val columns = when {
                isLargeCanvas -> 4
                profile.tier == DeviceProfileTier.TV_REGULAR -> 4
                else -> grid.columns
            }
            val topPadding = grid.topPadding
            val isPhoneLandscape = profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE
            val isTv66 = profile.tier == DeviceProfileTier.TV_LARGE
            val isTv42 = profile.tier == DeviceProfileTier.TV_REGULAR

            // Mismos márgenes que Products — el título dinámico debe caer en el mismo X.
            val horizontalPadding = grid.horizontalPadding
            val gridContentPadding = grid.contentPadding
            val gridMaxWidth = grid.maxContentWidth
            val badgeWidth = uiMetrics.badgeHeight * TreatmentUiMetrics.BADGE_WIDTH_TO_HEIGHT
            val titleStartGap = badgeWidth + when {
                isPhoneLandscape -> 48.dp
                isLargeCanvas -> 44.dp
                isTv66 -> 40.dp
                isTv42 -> 34.dp
                else -> 32.dp
            }

            // Gutters bien visibles en todos los perfiles.
            val cardSpacing = when {
                isLargeCanvas -> 32.dp
                profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE -> 14.dp
                profile.tier == DeviceProfileTier.TV_REGULAR -> 22.dp
                profile.tier == DeviceProfileTier.TV_LARGE -> 32.dp
                else -> 18.dp
            }

            // Mismo margen arriba y abajo (como antes del 15% — ese quedó excesivo).
            val claseTerapeuticaGap = when {
                isLargeCanvas -> 26.dp
                profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE -> 14.dp
                // Fire / Television_1080: menos aire vertical para que quepan 2 filas.
                profile.tier == DeviceProfileTier.TV_REGULAR -> 10.dp
                profile.tier == DeviceProfileTier.TV_LARGE -> 26.dp
                else -> if (profile.isWide) 18.dp else 14.dp
            }

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
                        titleStartGap = titleStartGap,
                        onBack = onBack,
                    )

                    Spacer(modifier = Modifier.height(claseTerapeuticaGap))

                    Text(
                        text = "Clase terapéutica",
                        fontSize = when {
                            isLargeCanvas && profile.tier == DeviceProfileTier.TV_LARGE -> 34.sp
                            isLargeCanvas -> 28.sp
                            profile.tier == DeviceProfileTier.TV_LARGE -> 34.sp
                            profile.tier == DeviceProfileTier.TV_REGULAR -> 26.sp
                            profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE -> 18.sp
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

                    Spacer(modifier = Modifier.height(claseTerapeuticaGap))

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
                                top = 0.dp,
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
                                    iconFill = uiMetrics.cardIconFill,
                                    aspectRatio = uiMetrics.cardAspectRatio,
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
    titleStartGap: Dp = 0.dp,
    onBack: () -> Unit,
) {
    // Misma geometría de header que Products: contentPadding + titleStartGap (hueco del badge).
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
            modifier = Modifier.padding(start = titleStartGap),
        )

        Spacer(modifier = Modifier.weight(1f))

        GreenNavButton(
            assetPath = "svg/ui/Before.svg",
            contentDescription = "Volver",
            onClick = onBack,
            size = navButtonSize,
        )
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
    onClick: () -> Unit,
) {
    val iconModel = TreatmentIconAssets.resolve(iconUrl = treatment.media.icono)
    val context = LocalContext.current
    val iconSizePx = with(LocalDensity.current) { iconSize.roundToPx() }
    val labelHeight = therapeuticClassLabelHeight(labelLineHeight)

    LaunchedEffect(treatment.id, iconModel) {
        android.util.Log.d(
            "TreatmentIcon",
            "render id=${treatment.id} name=${treatment.name} icon=$iconModel",
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
            .clickableWithSound { onClick() }
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (!iconModel.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(iconModel)
                        .size(iconSizePx)
                        .transformations(TrimTransparentTransformation())
                        .crossfade(true)
                        .build(),
                    contentDescription = treatment.name,
                    modifier = Modifier.fillMaxSize(iconFill),
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

/** Altura del rótulo (~2 líneas) — evita truncate en TV42/Fire. */
@Composable
private fun therapeuticClassLabelHeight(lineHeight: TextUnit): Dp {
    return with(LocalDensity.current) { (lineHeight.toPx() * 2.1f).toDp() }
}
