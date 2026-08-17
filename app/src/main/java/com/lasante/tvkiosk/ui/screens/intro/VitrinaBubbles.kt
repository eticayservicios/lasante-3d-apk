package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.ui.layout.Tv42Spacing
import com.lasante.tvkiosk.ui.utils.clickableWithSound
import com.lasante.tvkiosk.ui.utils.UiSound

/** Verde de la línea de Elementos Estrella (#2FAD11). */
private val StarLineGreen = Color(0xFF2FAD11)

/**
 * Degradado vertical del título (mock Elementos Estrella):
 * gris oscuro → gris medio (sin blanco puro: en fondo claro desaparecía).
 */
private val StarTitleBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF505050),
        Color(0xFF8A8A8A),
        Color(0xFFB8B8B8),
    ),
)

/** Fracción del ancho de rampa donde empieza la diagonal (asset Linea inicial / Hikvision). */
private const val RAMP_DIAG_START_FRACTION_DEFAULT = 0.88f

/** Altura de la rampa / ancho (265 px de subida útil sobre 1368 de ancho). */
private const val RAMP_RISE_OVER_WIDTH = 265f / 1368f

/**
 * Título Poppins + rampa verde en código (sin PNG),
 * burbujas centradas sobre la vitrina.
 *
 * Misma geometría en todos los perfiles (Hikvision / phone / TV42):
 * punto → horizontal larga (~88%) → diagonal corta (~12%) →
 * horizontal por centros de burbujas.
 * TV42: −8 espacios en X (línea+título); −N espacios de rise (diagonal).
 */
@Composable
fun VitrinaBubblesRow(
    slotProducts: Array<Product?>,
    visible: Boolean,
    metrics: IntroLayoutMetrics,
    onProductClick: (Product) -> Unit,
    onStarProductsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val alpha = rememberVitrinaInteractionAlpha(visible)
    if (alpha <= 0.01f) return

    val slotCount = VitrinaConstants.SLOTS_PER_UNIT
    val poppins = MaterialTheme.typography.bodyLarge.fontFamily

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .fillMaxWidth(),
    ) {
        val density = LocalDensity.current
        val rowWidth = maxWidth

        val clusterWidth = rowWidth * metrics.bubblesRowWidthFraction
        val totalSpacing = metrics.bubbleSpacing * (slotCount - 1)
        val sizeFromWidth = (clusterWidth - totalSpacing) / slotCount
        val bubbleSize = minOf(metrics.bubbleSize, sizeFromWidth)
        val bubblesContentWidth = bubbleSize * slotCount + totalSpacing
        val clusterStart = (rowWidth - clusterWidth) / 2f
        val bubblesStart = clusterStart + (clusterWidth - bubblesContentWidth) / 2f
        val bubblesEnd = bubblesStart + bubblesContentWidth

        // Misma geometría Hikvision; nudges TV42 viven en IntroLayoutMetrics.
        val rampStartX =
            metrics.bubblesBadgeCenterXInRow - metrics.socialIconSize / 2f -
                metrics.starRampStartNudge
        val rampWidth = metrics.starLineStartWidth
        val rampEndX = if (metrics.starRampAttachToFirstBubble) {
            (bubblesStart + metrics.starRampBubbleOverlap)
                .coerceAtLeast(rampStartX + 80.dp)
        } else {
            // TV66: base Hikvision + 1 espacio hacia la burbuja (pegar diagonal).
            (minOf(rampStartX + rampWidth, bubblesStart) + metrics.starRampBubbleOverlap)
                .coerceAtLeast(rampStartX + 80.dp)
        }
        val effectiveRampWidth = rampEndX - rampStartX
        val titleFontSize = metrics.starTitleFontSize
        val titleHeight = with(density) { titleFontSize.toDp() }
        var titleWidth by remember(titleFontSize) { mutableStateOf(0.dp) }

        // TV1080: diagonal corta. TV66 y resto: fracción Hikvision (0.88).
        val shortDiagRun = metrics.starRampShortDiagRun
        val riseFactor = metrics.starRampRiseFactor
        val rawRampRise = if (shortDiagRun > 0.dp) {
            shortDiagRun * riseFactor - metrics.starRampRiseNudge
        } else {
            effectiveRampWidth * RAMP_RISE_OVER_WIDTH - metrics.starRampRiseNudge
        }
        val rampRise =
            if (metrics.starRampRiseNudge > 0.dp || shortDiagRun > 0.dp) {
                rawRampRise.coerceAtLeast(
                    if (shortDiagRun > 0.dp) shortDiagRun * riseFactor else 14.dp,
                )
            } else {
                rawRampRise
            }

        val upperY = bubbleSize / 2f
        val lowerY = upperY + rampRise
        val rawTitleY = lowerY - titleHeight - metrics.starTitleLineGap
        val titleLift = if (rawTitleY < 0.dp) -rawTitleY else 0.dp
        val titleY = rawTitleY + titleLift
        val placedUpperY = upperY + titleLift
        val placedLowerY = lowerY + titleLift
        val bubbleTop = titleLift
        val barHeight = maxOf(
            bubbleTop + bubbleSize,
            placedLowerY + metrics.bubblesConnectorStroke * 4f + 4.dp,
            titleY + titleHeight + 4.dp,
        )

        val minLowerForTitle = if (titleWidth > 0.dp) {
            titleWidth + Tv42Spacing.spaces(1) + metrics.bubblesDotSize * 2f
        } else {
            80.dp
        }
        val diagStartX = if (shortDiagRun > 0.dp) {
            (rampEndX - shortDiagRun).coerceAtLeast(rampStartX + minLowerForTitle)
        } else {
            val diagStartFraction = metrics.starRampDiagStartFraction
                .coerceIn(0.50f, RAMP_DIAG_START_FRACTION_DEFAULT)
            (rampStartX + effectiveRampWidth * diagStartFraction + metrics.starRampLowerExtra -
                metrics.starRampDiagLeanLeft)
                .coerceAtMost((rampEndX - 14.dp).coerceAtLeast(rampStartX + 80.dp))
        }
        val lowerLineStartX = rampStartX - metrics.starRampDotExtendLeft
        val lowerSegWidth = diagStartX - lowerLineStartX
        val titleX = if (titleWidth > 0.dp) {
            lowerLineStartX + (lowerSegWidth - titleWidth) / 2f
        } else {
            lowerLineStartX
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = with(density) { metrics.bubblesConnectorStroke.toPx() }
                val dotRadius = stroke * 2.8f
                val startX = with(density) { lowerLineStartX.toPx() }
                val diagX = with(density) { diagStartX.toPx() }
                val endRampX = with(density) { rampEndX.toPx() }
                val endX = with(density) { bubblesEnd.toPx() }
                val lowY = with(density) { placedLowerY.toPx() }
                val highY = with(density) { placedUpperY.toPx() }

                drawCircle(
                    color = StarLineGreen,
                    radius = dotRadius,
                    center = Offset(startX, lowY),
                )
                drawLine(
                    color = StarLineGreen,
                    start = Offset(startX, lowY),
                    end = Offset(diagX, lowY),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = StarLineGreen,
                    start = Offset(diagX, lowY),
                    end = Offset(endRampX, highY),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                if (endX > endRampX) {
                    drawLine(
                        color = StarLineGreen,
                        start = Offset(endRampX, highY),
                        end = Offset(endX, highY),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = bubbleTop)
                    .fillMaxWidth(metrics.bubblesRowWidthFraction)
                    .height(bubbleSize),
                horizontalArrangement = Arrangement.spacedBy(
                    space = metrics.bubbleSpacing,
                    alignment = Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                slotProducts.forEach { product ->
                    if (product != null) {
                        VitrinaProductBubble(
                            product = product,
                            bubbleSize = bubbleSize,
                            productSizeFraction = metrics.bubbleProductSizeFraction,
                            onClick = { onProductClick(product) },
                        )
                    } else {
                        Spacer(modifier = Modifier.size(bubbleSize))
                    }
                }
            }

            Text(
                text = "PRODUCTOS ESTRELLAS",
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    brush = StarTitleBrush,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = titleFontSize,
                    lineHeight = titleFontSize,
                    letterSpacing = 0.4.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both,
                    ),
                ),
                color = Color.Unspecified,
                onTextLayout = { layout ->
                    titleWidth = with(density) { layout.size.width.toDp() }
                },
                modifier = Modifier
                    .offset(x = titleX, y = titleY)
                    .wrapContentWidth()
                    .height(titleHeight)
                    .wrapContentHeight(align = Alignment.Bottom),
            )
        }
    }
}

/**
 * Burbuja blanca con sombra interior grisácea (hacia dentro).
 * Centro claro → borde gris suave.
 */
@Composable
private fun VitrinaBubbleSphere(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.00f to Color(0xFFFFFFFF),
                    0.55f to Color(0xFFF4F4F4),
                    0.82f to Color(0xFFE4E4E4),
                    0.94f to Color(0xFFCBCBCB),
                    1.00f to Color(0xFFB4B4B4),
                ),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }
}

@Composable
private fun VitrinaProductBubble(
    product: Product,
    bubbleSize: Dp,
    productSizeFraction: Float,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val imageUrl = product.bubbleImageUrl()
    val productModel = remember(imageUrl, context) {
        imageUrl?.let {
            ImageRequest.Builder(context)
                .data(it)
                .crossfade(true)
                .build()
        }
    }

    Box(
        modifier = Modifier
            .size(bubbleSize)
            .clip(CircleShape)
            .clickableWithSound(sound = UiSound.Product, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        VitrinaBubbleSphere(modifier = Modifier.fillMaxSize())
        if (productModel != null) {
            Box(
                modifier = Modifier.fillMaxSize(productSizeFraction),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = productModel,
                    contentDescription = product.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

/** Miniatura o imagen principal para la burbuja del producto destacado. */
fun Product.bubbleImageUrl(): String? {
    val miniatura = media.imagenes2d.miniatura?.trim()?.takeIf { it.isNotBlank() }
    val principal = media.imagenes2d.principal?.trim()?.takeIf { it.isNotBlank() }
    return miniatura ?: principal
}
