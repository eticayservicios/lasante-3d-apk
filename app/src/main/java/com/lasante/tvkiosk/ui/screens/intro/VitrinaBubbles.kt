package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.ui.theme.LaSanteOrange

/** Verde del conector / badge (mockup). */
private val BubblesConnectorGreen = Color(0xFF88B72E)
private val BubblesBadgeGreenStart = Color(0xFF6FA320)
private val BubblesBadgeGreenEnd = Color(0xFFA4D23A)
/** Azul claro del puntito decorativo (mockup; no el azul de marca oscuro). */
private val BubblesDotBlue = Color(0xFF6295B9)

private val BubbleDotColors = listOf(
    BubblesConnectorGreen,
    LaSanteOrange,
    BubblesDotBlue,
)

/**
 * Badge a la izquierda (margen redes) + burbujas centradas en la vitrina como antes.
 * Línea solo hasta la última burbuja. Lógica de productos/slots sin cambios.
 */
@Composable
fun VitrinaBubblesRow(
    slotProducts: Array<Product?>,
    visible: Boolean,
    metrics: IntroLayoutMetrics,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = rememberVitrinaInteractionAlpha(visible)
    if (alpha <= 0.01f) return

    val slotCount = VitrinaConstants.SLOTS_PER_UNIT
    val badgeHeight = metrics.bubblesBadgeHeight
    val connectorStroke = metrics.bubblesConnectorStroke
    val dotSize = metrics.bubblesDotSize
    // Centro del badge = centro de la columna de redes (mitad de los iconos).
    val badgeCenterTargetX = metrics.bubblesBadgeCenterXInRow

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .fillMaxWidth(),
    ) {
        val density = LocalDensity.current
        val rowWidth = maxWidth
        var measuredBadgeWidth by remember { mutableStateOf(badgeHeight * 4.2f) }
        val badgeOffsetX = badgeCenterTargetX - measuredBadgeWidth / 2f

        // Burbujas: misma geometría que antes (fracción original, centradas en la vitrina).
        val clusterWidth = rowWidth * metrics.bubblesRowWidthFraction
        val totalSpacing = metrics.bubbleSpacing * (slotCount - 1)
        val sizeFromWidth = (clusterWidth - totalSpacing) / slotCount
        val bubbleSize = minOf(metrics.bubbleSize, sizeFromWidth)
        val bubblesContentWidth = bubbleSize * slotCount + totalSpacing
        val clusterStart = (rowWidth - clusterWidth) / 2f
        val bubblesStart = clusterStart + (clusterWidth - bubblesContentWidth) / 2f
        val bubblesEnd = bubblesStart + bubblesContentWidth

        // Badge puede ir a la izquierda del contenedor (offset negativo) para centrar en redes.
        val badgeEnd = badgeOffsetX + measuredBadgeWidth
        val lineStart = badgeEnd.coerceAtMost(bubblesStart)
        val lineEnd = bubblesEnd
        val barHeight = maxOf(bubbleSize, badgeHeight)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
        ) {
            // Línea: desde el badge hasta el borde derecho de la última burbuja (sin sobra).
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = with(density) { connectorStroke.toPx() }
                val y = size.height / 2f
                val startX = with(density) { lineStart.toPx() }
                val endX = with(density) { lineEnd.toPx() }
                if (endX > startX) {
                    drawLine(
                        color = BubblesConnectorGreen,
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
                }
            }

            // Puntitos solo en los gaps del cluster de burbujas.
            Canvas(modifier = Modifier.fillMaxSize()) {
                val bubblePx = with(density) { bubbleSize.toPx() }
                val spacingPx = with(density) { metrics.bubbleSpacing.toPx() }
                val dotPx = with(density) { dotSize.toPx() }
                val startX = with(density) { bubblesStart.toPx() }
                val y = size.height / 2f
                for (i in 0 until slotCount - 1) {
                    val leftCenter = startX + bubblePx * i + spacingPx * i + bubblePx / 2f
                    val rightCenter = leftCenter + bubblePx + spacingPx
                    drawCircle(
                        color = BubbleDotColors[i % BubbleDotColors.size],
                        radius = dotPx / 2f,
                        center = Offset((leftCenter + rightCenter) / 2f, y),
                    )
                }
            }

            // Burbujas centradas (posición histórica sobre la vitrina).
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
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

            // Badge centrado en la columna de redes (mitad de los iconos).
            ProductosEstrellasInlineBadge(
                height = badgeHeight,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = badgeOffsetX)
                    .onSizeChanged { measuredBadgeWidth = with(density) { it.width.toDp() } },
            )
        }
    }
}

@Composable
private fun ProductosEstrellasInlineBadge(
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val fontSize = when {
        height >= 60.dp -> 18.sp
        height >= 50.dp -> 16.sp
        height >= 42.dp -> 15.sp
        height >= 32.dp -> 12.sp
        height >= 26.dp -> 10.sp
        else -> 9.sp
    }
    Box(
        modifier = modifier
            .height(height)
            .wrapContentWidth()
            .clip(RoundedCornerShape(percent = 50))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(BubblesBadgeGreenStart, BubblesBadgeGreenEnd),
                ),
            )
            .padding(horizontal = height * 0.45f),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "PRODUCTOS ESTRELLAS",
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
        )
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
            .clickable(onClick = onClick),
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
